package com.novara.banking.deposit;

import com.novara.banking.audit.AuditService;
import com.novara.banking.deposit.DepositModels.*;
import com.novara.banking.identity.CurrentIdentity.FederatedIdentity;
import com.novara.banking.ledger.*;
import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;

@Service
public class DepositService {
    private static final String PRODUCT_SQL="""
      SELECT p.id,p.public_id,p.code,p.name,p.deposit_type,p.currency,p.minimum_amount_minor,p.maximum_amount_minor,
       p.minimum_tenure_months,p.maximum_tenure_months,p.interest_method,p.compounding_frequency,r.annual_rate_basis_points,
       r.policy_version,p.grace_period_days,p.premature_closure_allowed,p.renewal_allowed,p.version
      FROM deposit_products p JOIN deposit_product_rate_versions r ON r.product_id=p.id
      WHERE p.status='ACTIVE' AND CURRENT_DATE BETWEEN p.effective_from AND COALESCE(p.effective_to,CURRENT_DATE)
       AND CURRENT_DATE BETWEEN r.effective_from AND COALESCE(r.effective_to,CURRENT_DATE)
      """;
    private final JdbcClient jdbc; private final DepositInterestService interest; private final LedgerPostingService ledger; private final AuditService audit;
    public DepositService(JdbcClient jdbc,DepositInterestService interest,LedgerPostingService ledger,AuditService audit){this.jdbc=jdbc;this.interest=interest;this.ledger=ledger;this.audit=audit;}

    public List<Product> products(String type){
        String suffix=type==null||type.isBlank()?"":" AND p.deposit_type=:type";
        var query=jdbc.sql(PRODUCT_SQL+suffix+" ORDER BY p.deposit_type,p.name");
        if(!suffix.isEmpty())query=query.param("type",type.toUpperCase(Locale.ROOT));
        return query.query((r,n)->product(r)).list();
    }
    public Review review(FederatedIdentity identity,ReviewRequest request,String requiredType){
        customer(identity); ownedFunding(identity,request.fundingAccountId(),false); ProductRow p=productRow(request.productId()); validate(p,request,requiredType);
        LocalDate opening=LocalDate.now(), maturity=opening.plusMonths(request.tenureMonths());
        var projection="FIXED".equals(p.type)?interest.fixed(request.amountMinor(),p.rate,request.tenureMonths(),p.method):interest.recurring(request.amountMinor(),p.rate,request.tenureMonths(),p.method);
        long contribution="FIXED".equals(p.type)?request.amountMinor():Math.multiplyExact(request.amountMinor(),request.tenureMonths());
        List<ScheduleItem> schedule=new ArrayList<>();
        if("RECURRING".equals(p.type))for(int i=1;i<=request.tenureMonths();i++)schedule.add(new ScheduleItem(i,opening.plusMonths(i-1),request.amountMinor(),"PROJECTED",null));
        return new Review(p.publicId,p.name,p.type,request.amountMinor(),p.currency,request.tenureMonths(),p.rate,p.method,p.policyVersion,opening,maturity,
          projection.interestMinor(),projection.maturityMinor(),contribution,request.maturityInstruction(),schedule,
          "Sandbox projection using "+p.method+", ACTUAL_365 policy metadata and monthly contract periods. Returns are estimates until confirmation.");
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Deposit confirm(FederatedIdentity identity,ReviewRequest request,String requiredType,String key,String correlation){
        validateKey(key); String operation=requiredType+"_DEPOSIT_OPEN"; String actor=identity.subject(); String hash=hash(request,requiredType);
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:k,0))").param("k",actor+'|'+operation+'|'+key).query(Integer.class).single();
        var prior=jdbc.sql("SELECT request_hash,resource_public_id FROM idempotency_records WHERE principal_subject=:a AND operation_type=:o AND idempotency_key=:k")
          .param("a",actor).param("o",operation).param("k",key).query((r,n)->new Object[]{r.getString(1),r.getObject(2,UUID.class)}).optional();
        if(prior.isPresent()){
            if(!prior.get()[0].equals(hash))throw conflict("IDEMPOTENCY_KEY_REUSED","The idempotency key was used for a different deposit instruction.");
            return requireOwned(identity,(UUID)prior.get()[1]);
        }
        long customer=customer(identity); Funding funding=ownedFunding(identity,request.fundingAccountId(),true); ProductRow p=productRow(request.productId()); validate(p,request,requiredType);
        if(!funding.currency.equals(p.currency))throw invalid("CURRENCY_MISMATCH","The funding account currency does not match the product.");
        if(funding.availableMinor<request.amountMinor())throw invalid("INSUFFICIENT_FUNDS","The funding account has insufficient available funds.");
        Review review=review(identity,request,requiredType); UUID depositId=UUID.randomUUID(); String reference="DEP-"+depositId.toString().substring(0,8).toUpperCase(Locale.ROOT);
        jdbc.sql("INSERT INTO idempotency_records(principal_subject,operation_type,idempotency_key,request_hash,resource_type,resource_public_id,expires_at) VALUES(:a,:o,:k,:h,'DEPOSIT',:id,now()+interval '7 days')")
          .param("a",actor).param("o",operation).param("k",key).param("h",hash).param("id",depositId).update();
        long depositLedger=jdbc.sql("INSERT INTO ledger_accounts(code,name,classification,currency) VALUES(:code,:name,'LIABILITY',:currency) RETURNING id")
          .param("code","DEP-"+depositId).param("name",reference+" customer deposit liability").param("currency",p.currency).query(Long.class).single();
        jdbc.sql("INSERT INTO ledger_balances(ledger_account_id) VALUES(:id)").param("id",depositLedger).update();
        LocalDate nextDue="RECURRING".equals(p.type)?review.openingDate().plusMonths(1):null;
        long depositInternal=jdbc.sql("""
          INSERT INTO deposit_accounts(public_id,reference,customer_id,product_id,funding_account_id,ledger_account_id,deposit_type,status,currency,principal_minor,total_contributed_minor,opening_date,maturity_date,next_due_date)
          VALUES(:public,:ref,:customer,:product,:funding,:ledger,:type,'ACTIVE',:currency,:principal,:contributed,:opening,:maturity,:next) RETURNING id
          """).param("public",depositId).param("ref",reference).param("customer",customer).param("product",p.id).param("funding",funding.id).param("ledger",depositLedger)
          .param("type",p.type).param("currency",p.currency).param("principal",request.amountMinor()).param("contributed",request.amountMinor())
          .param("opening",review.openingDate()).param("maturity",review.maturityDate()).param("next",nextDue,java.sql.Types.DATE).query(Long.class).single();
        jdbc.sql("""
          INSERT INTO deposit_contract_terms(deposit_account_id,product_code,product_name,product_version,rate_policy_version,annual_rate_basis_points,tenure_months,interest_method,day_count_convention,compounding_frequency,maturity_instruction,installment_amount_minor,projected_interest_minor,projected_maturity_minor)
          VALUES(:id,:code,:name,:version,:policy,:rate,:tenure,:method,'ACTUAL_365',:compound,'CREDIT_LINKED_ACCOUNT',:installment,:interest,:maturity)
          """).param("id",depositInternal).param("code",p.code).param("name",p.name).param("version",p.version).param("policy",p.policyVersion).param("rate",p.rate)
          .param("tenure",request.tenureMonths()).param("method",p.method).param("compound",p.compounding).param("installment","RECURRING".equals(p.type)?request.amountMinor():null,java.sql.Types.BIGINT)
          .param("interest",review.projectedInterestMinor()).param("maturity",review.projectedMaturityMinor()).update();
        var posting=ledger.post(new JournalCommand("BATCH-"+reference+"-OPEN",reference+"-OPEN",p.currency,p.type+" deposit opening",List.of(
          new JournalLineCommand(funding.ledgerId,JournalLineCommand.Direction.DEBIT,request.amountMinor(),p.currency),
          new JournalLineCommand(depositLedger,JournalLineCommand.Direction.CREDIT,request.amountMinor(),p.currency)),null));
        jdbc.sql("INSERT INTO deposit_posting_references(deposit_account_id,operation_type,operation_key,journal_id) VALUES(:id,'OPENING',:key,:journal)")
          .param("id",depositInternal).param("key",key).param("journal",posting.journalId()).update();
        jdbc.sql("INSERT INTO deposit_status_events(deposit_account_id,new_status,reason_code) VALUES(:id,'ACTIVE','CUSTOMER_CONFIRMED')").param("id",depositInternal).update();
        if("RECURRING".equals(p.type)){
          jdbc.sql("INSERT INTO deposit_installment_occurrences(deposit_account_id,sequence_number,scheduled_date,amount_minor,currency,status,idempotency_key,attempt_count,executed_at,journal_id) VALUES(:id,1,:date,:amount,:currency,'COMPLETED',:key,1,now(),:journal)")
            .param("id",depositInternal).param("date",review.openingDate()).param("amount",request.amountMinor()).param("currency",p.currency).param("key","RD-"+depositId+"-1").param("journal",posting.journalId()).update();
          for(int sequence=2;sequence<=request.tenureMonths();sequence++)jdbc.sql("""
          INSERT INTO deposit_installment_occurrences(deposit_account_id,sequence_number,scheduled_date,amount_minor,currency,status,idempotency_key)
          VALUES(:id,:seq,:date,:amount,:currency,'SCHEDULED',:key)
          """).param("id",depositInternal).param("seq",sequence).param("date",review.openingDate().plusMonths(sequence-1)).param("amount",request.amountMinor()).param("currency",p.currency).param("key","RD-"+depositId+"-"+sequence).update();
        }
        jdbc.sql("UPDATE idempotency_records SET response_status=201 WHERE principal_subject=:a AND operation_type=:o AND idempotency_key=:k").param("a",actor).param("o",operation).param("k",key).update();
        audit.record(actor,p.type+"_DEPOSIT_OPENED","DEPOSIT",depositId,"SUCCESS",correlation,"{\"ownershipVerified\":true,\"stepUpVerified\":true,\"sandboxProduct\":true}");
        return requireOwned(identity,depositId);
    }

    public List<Deposit> list(FederatedIdentity identity){return jdbc.sql(DEPOSIT_VIEW+" WHERE d.customer_id=:customer "+DEPOSIT_GROUP+" ORDER BY d.created_at DESC").param("customer",customer(identity)).query(this::mapDeposit).list();}
    public Deposit requireOwned(FederatedIdentity identity,UUID id){return jdbc.sql(DEPOSIT_VIEW+" WHERE d.customer_id=:customer AND d.public_id=:id "+DEPOSIT_GROUP).param("customer",customer(identity)).param("id",id).query(this::mapDeposit).optional().orElseThrow(this::notFound);}
    public List<ScheduleItem> schedule(FederatedIdentity identity,UUID id){Deposit d=requireOwned(identity,id);return jdbc.sql("SELECT o.sequence_number,o.scheduled_date,o.amount_minor,o.status,o.failure_reason FROM deposit_installment_occurrences o JOIN deposit_accounts d ON d.id=o.deposit_account_id WHERE d.public_id=:id ORDER BY o.sequence_number").param("id",d.id()).query((r,n)->new ScheduleItem(r.getInt(1),r.getObject(2,LocalDate.class),r.getLong(3),r.getString(4),r.getString(5))).list();}

    private ProductRow productRow(UUID id){return jdbc.sql(PRODUCT_SQL+" AND p.public_id=:id").param("id",id).query((r,n)->productRow(r,n)).optional().orElseThrow(()->new DomainException(HttpStatus.NOT_FOUND,"DEPOSIT_PRODUCT_NOT_FOUND","The deposit product is unavailable."));}
    private Product product(ResultSet r)throws SQLException{ProductRow p=productRow(r,0);return new Product(p.publicId,p.code,p.name,p.type,p.currency,p.min,p.max,p.minMonths,p.maxMonths,p.method,p.compounding,p.rate,p.policyVersion,p.graceDays,p.premature,p.renewal,true);}
    private ProductRow productRow(ResultSet r,int n)throws SQLException{return new ProductRow(r.getLong(1),r.getObject(2,UUID.class),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getLong(7),r.getLong(8),r.getInt(9),r.getInt(10),r.getString(11),r.getString(12),r.getInt(13),r.getString(14),r.getInt(15),r.getBoolean(16),r.getBoolean(17),r.getLong(18));}
    private void validate(ProductRow p,ReviewRequest r,String type){if(!p.type.equals(type))throw invalid("PRODUCT_TYPE_MISMATCH","The selected product does not support this workflow.");if(r.amountMinor()<p.min||r.amountMinor()>p.max)throw invalid("AMOUNT_OUT_OF_RANGE","The amount is outside the product's allowable range.");if(r.tenureMonths()<p.minMonths||r.tenureMonths()>p.maxMonths)throw invalid("TENURE_OUT_OF_RANGE","The tenure is outside the product's allowable range.");if(!"CREDIT_LINKED_ACCOUNT".equals(r.maturityInstruction()))throw invalid("UNSUPPORTED_MATURITY_INSTRUCTION","Only credit to the linked funding account is currently supported.");}
    private long customer(FederatedIdentity i){return jdbc.sql("SELECT id FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE'").param("issuer",i.issuer()).param("subject",i.subject()).query(Long.class).optional().orElseThrow(this::notFound);}
    private Funding ownedFunding(FederatedIdentity i,UUID id,boolean lock){String sql="""
      SELECT a.id,a.ledger_account_id,a.currency,b.balance_minor-a.minimum_balance_minor+a.overdraft_limit_minor-
       COALESCE((SELECT sum(h.amount_minor) FROM account_holds h WHERE h.account_id=a.id AND h.status='ACTIVE' AND h.expires_at>now()),0)
      FROM bank_accounts a JOIN account_owners o ON o.account_id=a.id JOIN customers c ON c.id=o.customer_id JOIN ledger_balances b ON b.ledger_account_id=a.ledger_account_id
       WHERE a.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject AND c.status='ACTIVE' AND o.can_debit=true AND a.status='ACTIVE' AND a.debit_restricted=false
      """+(lock?" FOR UPDATE OF a,b":"");return jdbc.sql(sql).param("id",id).param("issuer",i.issuer()).param("subject",i.subject()).query((r,n)->new Funding(r.getLong(1),r.getLong(2),r.getString(3),r.getLong(4))).optional().orElseThrow(()->new DomainException(HttpStatus.NOT_FOUND,"FUNDING_ACCOUNT_NOT_FOUND","The funding account is unavailable."));}
    private Deposit mapDeposit(ResultSet r,int n)throws SQLException{return new Deposit(r.getObject(1,UUID.class),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getLong(7),r.getLong(8),r.getLong(9),r.getInt(10),r.getString(11),r.getString(12),r.getObject(13,LocalDate.class),r.getObject(14,LocalDate.class),r.getObject(15,LocalDate.class),r.getInt(16),r.getLong(17),r.getLong(18),r.getString(19),r.getString(20),r.getInt(21),r.getInt(22),r.getInt(23));}
    private void validateKey(String k){if(k==null||!k.matches("[A-Za-z0-9_-]{16,100}"))throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","A valid Idempotency-Key is required.");}
    private String hash(ReviewRequest r,String t){try{String value=t+'|'+r.productId()+'|'+r.amountMinor()+'|'+r.tenureMonths()+'|'+r.fundingAccountId()+'|'+r.maturityInstruction();return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private DomainException invalid(String c,String m){return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,c,m);} private DomainException conflict(String c,String m){return new DomainException(HttpStatus.CONFLICT,c,m);} private DomainException notFound(){return new DomainException(HttpStatus.NOT_FOUND,"DEPOSIT_NOT_FOUND","The deposit was not found.");}
    private record ProductRow(long id,UUID publicId,String code,String name,String type,String currency,long min,long max,int minMonths,int maxMonths,String method,String compounding,int rate,String policyVersion,int graceDays,boolean premature,boolean renewal,long version){}
    private record Funding(long id,long ledgerId,String currency,long availableMinor){}
    private static final String DEPOSIT_VIEW="""
      SELECT d.public_id,d.reference,t.product_name,d.deposit_type,d.status,d.currency,d.principal_minor,d.total_contributed_minor,d.accrued_interest_minor,
       t.annual_rate_basis_points,t.rate_policy_version,t.interest_method,d.opening_date,d.maturity_date,d.next_due_date,t.tenure_months,t.projected_interest_minor,t.projected_maturity_minor,
       a.masked_number,t.maturity_instruction,
       count(o.id) FILTER(WHERE o.status='COMPLETED'),count(o.id) FILTER(WHERE o.status IN ('SCHEDULED','PROCESSING')),count(o.id) FILTER(WHERE o.status IN ('MISSED','FAILED'))
      FROM deposit_accounts d JOIN deposit_contract_terms t ON t.deposit_account_id=d.id JOIN bank_accounts a ON a.id=d.funding_account_id LEFT JOIN deposit_installment_occurrences o ON o.deposit_account_id=d.id
      """;
    private static final String DEPOSIT_GROUP=" GROUP BY d.id,t.deposit_account_id,a.id";
}
