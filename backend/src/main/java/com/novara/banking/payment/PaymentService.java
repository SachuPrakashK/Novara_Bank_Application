package com.novara.banking.payment;

import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity.FederatedIdentity;
import com.novara.banking.shared.DomainException;
import com.novara.banking.transfer.InternalTransferRequest;
import com.novara.banking.transfer.InternalTransferService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Service
public class PaymentService {
    private static final String METHOD="INTERNAL", CURRENCY="INR";
    private final JdbcClient jdbc; private final InternalTransferService transfers; private final AuditService audit;
    private final long defaultPerPayment; private final long defaultDaily; private final long stepUpThreshold;
    private final ZoneId bankingZone;

    public PaymentService(JdbcClient jdbc, InternalTransferService transfers, AuditService audit,
      @Value("${novara.payments.limits.per-payment-minor:50000000}") long defaultPerPayment,
      @Value("${novara.payments.limits.daily-minor:100000000}") long defaultDaily,
      @Value("${novara.security.step-up-transfer-threshold-minor}") long stepUpThreshold,
      @Value("${novara.payments.banking-timezone:Asia/Kolkata}") String bankingTimezone) {
        this.jdbc=jdbc;this.transfers=transfers;this.audit=audit;this.defaultPerPayment=defaultPerPayment;
        this.defaultDaily=defaultDaily;this.stepUpThreshold=stepUpThreshold;this.bankingZone=ZoneId.of(bankingTimezone);
    }

    @Transactional
    public PaymentView createIntent(FederatedIdentity identity, CreatePaymentIntentRequest request, String correlationId) {
        validateBasics(request.amountMinor(),request.currency(),request.method());
        Customer customer=requireCustomer(identity); Account source=requireOwnedAccount(customer.id(),request.sourceAccountId(),true);
        Destination destination=resolveDestination(customer.id(),request.destinationType(),request.destinationId(),true);
        if(source.id()==destination.accountId()) throw invalid("SAME_ACCOUNT","Source and destination accounts must differ.");
        if(!source.currency().equals(destination.currency())) throw invalid("CURRENCY_MISMATCH","Source and destination currencies must match.");
        enforceStaticLimit(customer.id(),source.id(),request.amountMinor());
        UUID id=UUID.randomUUID(); Instant expires=Instant.now().plus(Duration.ofMinutes(5));
        long internal=jdbc.sql("""
          INSERT INTO payment_intents(public_id,customer_id,source_account_id,destination_account_id,beneficiary_id,destination_type,
            amount_minor,currency,method,description,requires_step_up,expires_at)
          VALUES(:public,:customer,:source,:destination,:beneficiary,CAST(:type AS payment_destination_type),:amount,'INR','INTERNAL',:description,:stepUp,:expires)
          RETURNING id
          """).param("public",id).param("customer",customer.id()).param("source",source.id()).param("destination",destination.accountId())
          .param("beneficiary",destination.beneficiaryId(),java.sql.Types.BIGINT).param("type",request.destinationType().name())
          .param("amount",request.amountMinor()).param("description",blank(request.description())).param("stepUp",request.amountMinor()>=stepUpThreshold)
          .param("expires",Timestamp.from(expires)).query(Long.class).single();
        addEvent(internal,"PENDING_AUTHORIZATION",null);
        audit.record(identity.subject(),"PAYMENT_INTENT_CREATED","PAYMENT",id,"SUCCESS",correlationId,"{\"serverAuthorizedReview\":true}");
        return requireOwned(identity,id);
    }

    @Transactional
    public PaymentView confirm(FederatedIdentity identity, UUID paymentId, String key, String correlationId) {
        validateKey(key); Customer customer=requireCustomer(identity);
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:key,0))").param("key",identity.subject()+"|PAYMENT_CONFIRM|"+key).query(Integer.class).single();
        String requestHash=sha(paymentId.toString());
        var prior=jdbc.sql("SELECT request_hash,resource_public_id FROM idempotency_records WHERE principal_subject=:subject AND operation_type='PAYMENT_CONFIRM' AND idempotency_key=:key")
          .param("subject",identity.subject()).param("key",key).query((rs,n)->new String[]{rs.getString(1),rs.getString(2)}).optional();
        if(prior.isPresent()) {
            if(!prior.get()[0].equals(requestHash)) throw invalid(HttpStatus.CONFLICT,"IDEMPOTENCY_KEY_REUSED","The idempotency key was already used for another payment.");
            return requireOwned(identity,UUID.fromString(prior.get()[1]));
        }
        Intent intent=lockIntent(customer.id(),paymentId);
        if(intent.status().equals("COMPLETED")) return requireOwned(identity,paymentId);
        if(!intent.status().equals("PENDING_AUTHORIZATION")) throw invalid(HttpStatus.CONFLICT,"INVALID_PAYMENT_STATE","The payment cannot be confirmed from its current state.");
        if(intent.expiresAt().isBefore(Instant.now())) { transition(intent.id(),"FAILED","INTENT_EXPIRED"); throw invalid("INTENT_EXPIRED","Create a new review before confirming this payment."); }
        revalidateDestination(intent);
        lockAndEnforceLimits(customer.id(),intent.sourceId(),intent.amount());
        jdbc.sql("INSERT INTO idempotency_records(principal_subject,operation_type,idempotency_key,request_hash,resource_type,resource_public_id,expires_at) VALUES(:subject,'PAYMENT_CONFIRM',:key,:hash,'PAYMENT',:payment,now()+interval '7 days')")
          .param("subject",identity.subject()).param("key",key).param("hash",requestHash).param("payment",paymentId).update();
        transition(intent.id(),"PROCESSING",null);
        var transfer=transfers.execute(identity.subject(),stableTransferKey(paymentId),new InternalTransferRequest(intent.sourcePublic(),intent.destinationPublic(),intent.amount(),CURRENCY,intent.description()),correlationId);
        long transferId=jdbc.sql("SELECT id FROM transfers WHERE public_id=:id").param("id",transfer.id()).query(Long.class).single();
        jdbc.sql("UPDATE payment_intents SET status='COMPLETED',transfer_id=:transfer,confirmed_at=now(),completed_at=now(),updated_at=now() WHERE id=:id")
          .param("transfer",transferId).param("id",intent.id()).update();
        addEvent(intent.id(),"COMPLETED",null);
        jdbc.sql("UPDATE idempotency_records SET response_status=200 WHERE principal_subject=:subject AND operation_type='PAYMENT_CONFIRM' AND idempotency_key=:key")
          .param("subject",identity.subject()).param("key",key).update();
        audit.record(identity.subject(),"PAYMENT_COMPLETED","PAYMENT",paymentId,"SUCCESS",correlationId,"{\"limitsVerified\":true,\"ledgerPosted\":true}");
        return requireOwned(identity,paymentId);
    }

    public PaymentView requireOwned(FederatedIdentity identity, UUID id) {
        return jdbc.sql(VIEW_SQL+" WHERE p.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject")
          .param("id",id).param("issuer",identity.issuer()).param("subject",identity.subject()).query(this::map).optional().orElseThrow(this::notFound);
    }

    public PaymentPage list(FederatedIdentity identity,String status,String type,String query,int page,int size) {
        int safePage=Math.max(0,page),safeSize=Math.min(50,Math.max(1,size)); String safeStatus=status==null?"":status.toUpperCase(Locale.ROOT);
        String safeType=type==null?"":type.toUpperCase(Locale.ROOT); String q=query==null?"":query.trim();
        String where=" WHERE c.identity_issuer=:issuer AND c.identity_subject=:subject AND (:status='' OR p.status::text=:status) AND (:type='' OR p.destination_type::text=:type) AND (:q='' OR lower(COALESCE(b.name,d.nickname,'')) LIKE lower('%'||:q||'%') OR lower(COALESCE(j.journal_reference,'')) LIKE lower('%'||:q||'%'))";
        var spec=jdbc.sql(VIEW_SQL+where+" ORDER BY p.created_at DESC,p.public_id LIMIT :size OFFSET :offset")
          .param("issuer",identity.issuer()).param("subject",identity.subject()).param("status",safeStatus).param("type",safeType).param("q",q).param("size",safeSize).param("offset",safePage*safeSize);
        var items=spec.query(this::map).list();
        long total=jdbc.sql("SELECT count(*) FROM payment_intents p JOIN customers c ON c.id=p.customer_id LEFT JOIN beneficiaries b ON b.id=p.beneficiary_id LEFT JOIN bank_accounts d ON d.id=p.destination_account_id LEFT JOIN transfers t ON t.id=p.transfer_id LEFT JOIN journals j ON j.id=t.journal_id"+where)
          .param("issuer",identity.issuer()).param("subject",identity.subject()).param("status",safeStatus).param("type",safeType).param("q",q).query(Long.class).single();
        return new PaymentPage(items,safePage,safeSize,total);
    }

    public PaymentLimitView limits(FederatedIdentity identity) {
        Customer customer=requireCustomer(identity); Limits limits=effectiveLimits(customer.id(),null); long used=usedToday(customer.id()); long daily=limits.effectiveDaily();
        return new PaymentLimitView(limits.perPayment(),daily,used,Math.max(0,daily-used),limits.selectedDaily(),CURRENCY,bankingZone.getId());
    }

    @Transactional
    public PaymentLimitView lowerCustomerLimit(FederatedIdentity identity,long requested,String correlationId) {
        Customer customer=requireCustomer(identity); Limits current=effectiveLimits(customer.id(),null);
        if(requested>current.effectiveDaily()) throw invalid(HttpStatus.FORBIDDEN,"LIMIT_INCREASE_REQUIRES_REVIEW","Limit increases require provider-backed authorization and bank review.");
        jdbc.sql("""
          INSERT INTO payment_limit_policies(customer_id,method,per_payment_minor,daily_minor,customer_selected_daily_minor)
          VALUES(:customer,'INTERNAL',:per,:daily,:selected)
          ON CONFLICT (customer_id,method) WHERE customer_id IS NOT NULL AND account_id IS NULL
          DO UPDATE SET customer_selected_daily_minor=EXCLUDED.customer_selected_daily_minor,effective_at=now()
          """).param("customer",customer.id()).param("per",current.perPayment()).param("daily",current.bankDaily()).param("selected",requested).update();
        audit.record(identity.subject(),"PAYMENT_LIMIT_LOWERED","CUSTOMER",customer.publicId(),"SUCCESS",correlationId,"{\"increase\":false}");
        return limits(identity);
    }

    private void lockAndEnforceLimits(long customerId,long accountId,long amount) {
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(:id)").param("id",customerId).query(Integer.class).single();
        Limits limits=effectiveLimits(customerId,accountId); long used=usedToday(customerId);
        if(amount>limits.perPayment()) throw invalid("PER_PAYMENT_LIMIT_EXCEEDED","The payment exceeds the permitted per-payment limit.");
        if(amount>limits.effectiveDaily()-used) throw invalid("DAILY_LIMIT_EXCEEDED","The payment exceeds the remaining daily limit.");
    }
    private void enforceStaticLimit(long customerId,long accountId,long amount){Limits l=effectiveLimits(customerId,accountId);if(amount>l.perPayment())throw invalid("PER_PAYMENT_LIMIT_EXCEEDED","The payment exceeds the permitted per-payment limit.");}
    private Limits effectiveLimits(long customerId,Long accountId) {
        var row=jdbc.sql("SELECT per_payment_minor,daily_minor,customer_selected_daily_minor FROM payment_limit_policies WHERE customer_id=:customer AND account_id IS NULL AND method='INTERNAL'")
          .param("customer",customerId).query((rs,n)->new Limits(rs.getLong(1),rs.getLong(2),(Long)rs.getObject(3))).optional().orElse(new Limits(defaultPerPayment,defaultDaily,null));
        if(accountId==null)return row;
        var account=jdbc.sql("SELECT per_payment_minor,daily_minor FROM payment_limit_policies WHERE account_id=:account AND method='INTERNAL'").param("account",accountId)
          .query((rs,n)->new long[]{rs.getLong(1),rs.getLong(2)}).optional();
        return account.map(a->new Limits(Math.min(row.perPayment(),a[0]),Math.min(row.bankDaily(),a[1]),row.selectedDaily())).orElse(row);
    }
    private long usedToday(long customerId){Instant start=LocalDate.now(bankingZone).atStartOfDay(bankingZone).toInstant();Instant end=LocalDate.now(bankingZone).plusDays(1).atStartOfDay(bankingZone).toInstant();return jdbc.sql("SELECT COALESCE(sum(amount_minor+fee_minor),0) FROM payment_intents WHERE customer_id=:customer AND status='COMPLETED' AND completed_at>=:start AND completed_at<:end")
      .param("customer",customerId).param("start",Timestamp.from(start)).param("end",Timestamp.from(end)).query(Long.class).single();}

    private Customer requireCustomer(FederatedIdentity i){return jdbc.sql("SELECT id,public_id FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE'").param("issuer",i.issuer()).param("subject",i.subject()).query((rs,n)->new Customer(rs.getLong(1),rs.getObject(2,UUID.class))).optional().orElseThrow(()->invalid(HttpStatus.FORBIDDEN,"CUSTOMER_NOT_MAPPED","The authenticated identity is not mapped to an active customer."));}
    private Account requireOwnedAccount(long customer,UUID id,boolean debit){return jdbc.sql("SELECT a.id,a.public_id,a.nickname,a.masked_number,a.currency,a.status::text,a.debit_restricted,a.credit_restricted FROM bank_accounts a JOIN account_owners o ON o.account_id=a.id WHERE o.customer_id=:customer AND a.public_id=:id AND (:debit=false OR o.can_debit=true)").param("customer",customer).param("id",id).param("debit",debit).query((rs,n)->new Account(rs.getLong(1),rs.getObject(2,UUID.class),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getBoolean(7),rs.getBoolean(8))).optional().filter(a->a.status().equals("ACTIVE")&&(!debit||!a.debitRestricted())).orElseThrow(()->notFound());}
    private Destination resolveDestination(long customer,CreatePaymentIntentRequest.DestinationType type,UUID id,boolean enforceCooling){
        if(type==CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT){Account a=requireOwnedAccount(customer,id,false);if(a.creditRestricted())throw invalid("ACCOUNT_RESTRICTED","The destination is not eligible for credits.");return new Destination(a.id(),a.publicId(),null,a.nickname(),a.masked(),a.currency());}
        return jdbc.sql("""
          SELECT a.id,a.public_id,b.id,b.name,b.masked_account,a.currency FROM beneficiaries b JOIN bank_accounts a ON a.id=b.resolved_account_id
          WHERE b.customer_id=:customer AND b.public_id=:id AND b.status='ACTIVE' AND b.verified_at IS NOT NULL
            AND (:cooling=false OR b.cooling_until<=now()) AND a.status='ACTIVE' AND a.credit_restricted=false AND b.transfer_method='INTERNAL'
          """).param("customer",customer).param("id",id).param("cooling",enforceCooling).query((rs,n)->new Destination(rs.getLong(1),rs.getObject(2,UUID.class),rs.getLong(3),rs.getString(4),rs.getString(5),rs.getString(6))).optional()
          .orElseThrow(()->invalid("BENEFICIARY_INELIGIBLE","The beneficiary is not verified, resolved, or currently eligible for payment."));
    }
    private Intent lockIntent(long customer,UUID id){return jdbc.sql("SELECT p.id,p.status::text,p.source_account_id,s.public_id,p.destination_account_id,d.public_id,p.beneficiary_id,p.destination_type::text,p.amount_minor,p.description,p.expires_at FROM payment_intents p JOIN bank_accounts s ON s.id=p.source_account_id JOIN bank_accounts d ON d.id=p.destination_account_id WHERE p.customer_id=:customer AND p.public_id=:id FOR UPDATE OF p")
      .param("customer",customer).param("id",id).query((rs,n)->new Intent(rs.getLong(1),rs.getString(2),rs.getLong(3),rs.getObject(4,UUID.class),rs.getLong(5),rs.getObject(6,UUID.class),(Long)rs.getObject(7),rs.getString(8),rs.getLong(9),rs.getString(10),rs.getTimestamp(11).toInstant())).optional().orElseThrow(this::notFound);}
    private void revalidateDestination(Intent i){if(i.beneficiaryId()!=null){long count=jdbc.sql("SELECT count(*) FROM beneficiaries WHERE id=:id AND resolved_account_id=:account AND status='ACTIVE' AND verified_at IS NOT NULL AND cooling_until<=now() AND transfer_method='INTERNAL'").param("id",i.beneficiaryId()).param("account",i.destinationId()).query(Long.class).single();if(count!=1)throw invalid("BENEFICIARY_INELIGIBLE","The beneficiary is no longer eligible for payment.");}}
    private void transition(long id,String status,String reason){jdbc.sql("UPDATE payment_intents SET status=CAST(:status AS payment_status),updated_at=now() WHERE id=:id").param("status",status).param("id",id).update();addEvent(id,status,reason);}
    private void addEvent(long id,String status,String reason){jdbc.sql("INSERT INTO payment_status_events(payment_intent_id,status,reason_code) VALUES(:id,CAST(:status AS payment_status),:reason)").param("id",id).param("status",status).param("reason",reason,java.sql.Types.VARCHAR).update();}
    private PaymentView map(ResultSet rs,int n)throws SQLException{UUID id=rs.getObject(1,UUID.class);return new PaymentView(id,rs.getString(2),rs.getString(3),rs.getObject(4,UUID.class),rs.getString(5),rs.getString(6),rs.getObject(7,UUID.class),rs.getString(8),rs.getString(9),rs.getLong(10),rs.getLong(11),rs.getLong(10)+rs.getLong(11),rs.getString(12),rs.getString(13),rs.getString(14),rs.getBoolean(15),rs.getString(16),rs.getTimestamp(17).toInstant(),rs.getTimestamp(18).toInstant(),rs.getTimestamp(19)==null?null:rs.getTimestamp(19).toInstant(),timeline(id));}
    private List<PaymentView.StatusEvent> timeline(UUID id){return jdbc.sql("SELECT e.status::text,e.reason_code,e.occurred_at FROM payment_status_events e JOIN payment_intents p ON p.id=e.payment_intent_id WHERE p.public_id=:id ORDER BY e.occurred_at,e.id").param("id",id).query((rs,n)->new PaymentView.StatusEvent(rs.getString(1),rs.getString(2),rs.getTimestamp(3).toInstant())).list();}
    private void validateBasics(long amount,String currency,String method){if(amount<=0)throw invalid("INVALID_AMOUNT","Payment amount must be positive.");if(!CURRENCY.equals(currency))throw invalid("UNSUPPORTED_CURRENCY","Only INR is supported.");if(!METHOD.equals(method))throw invalid("PAYMENT_METHOD_UNAVAILABLE","External payment methods are not connected. Choose a Novara internal transfer.");}
    private void validateKey(String key){if(key==null||!key.matches("[A-Za-z0-9_-]{16,100}"))throw invalid(HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","A valid Idempotency-Key header is required.");}
    private String stableTransferKey(UUID id){return "payment_"+id.toString().replace("-","");}
    private String sha(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private String blank(String s){return s==null||s.isBlank()?null:s.trim();}
    private DomainException notFound(){return invalid(HttpStatus.NOT_FOUND,"PAYMENT_NOT_FOUND","Payment was not found.");}
    private DomainException invalid(String code,String message){return invalid(HttpStatus.UNPROCESSABLE_ENTITY,code,message);}
    private DomainException invalid(HttpStatus status,String code,String message){return new DomainException(status,code,message);}
    private record Customer(long id,UUID publicId){} private record Account(long id,UUID publicId,String nickname,String masked,String currency,String status,boolean debitRestricted,boolean creditRestricted){}
    private record Destination(long accountId,UUID accountPublicId,Long beneficiaryId,String name,String masked,String currency){}
    private record Intent(long id,String status,long sourceId,UUID sourcePublic,long destinationId,UUID destinationPublic,Long beneficiaryId,String destinationType,long amount,String description,Instant expiresAt){}
    private record Limits(long perPayment,long bankDaily,Long selectedDaily){long effectiveDaily(){return selectedDaily==null?bankDaily:Math.min(bankDaily,selectedDaily);}}
    private static final String VIEW_SQL="""
      SELECT p.public_id,p.status::text,p.destination_type::text,s.public_id,s.nickname,s.masked_number,
        CASE WHEN p.destination_type='BENEFICIARY' THEN b.public_id ELSE d.public_id END,
        CASE WHEN p.destination_type='BENEFICIARY' THEN b.name ELSE d.nickname END,
        CASE WHEN p.destination_type='BENEFICIARY' THEN b.masked_account ELSE d.masked_number END,
        p.amount_minor,p.fee_minor,p.currency,p.method,p.description,p.requires_step_up,j.journal_reference,p.created_at,p.expires_at,p.completed_at
      FROM payment_intents p JOIN customers c ON c.id=p.customer_id JOIN bank_accounts s ON s.id=p.source_account_id
      JOIN bank_accounts d ON d.id=p.destination_account_id LEFT JOIN beneficiaries b ON b.id=p.beneficiary_id LEFT JOIN transfers t ON t.id=p.transfer_id LEFT JOIN journals j ON j.id=t.journal_id
      """;
}
