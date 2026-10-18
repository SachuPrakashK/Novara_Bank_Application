package com.novara.banking.payment;

import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity.FederatedIdentity;
import com.novara.banking.shared.DomainException;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class PaymentScheduleService {
    private static final Logger log=LoggerFactory.getLogger(PaymentScheduleService.class);
    private final JdbcClient jdbc;private final PaymentService payments;private final AuditService audit;private final TransactionTemplate transactions;
    public PaymentScheduleService(JdbcClient jdbc,PaymentService payments,AuditService audit,PlatformTransactionManager transactionManager){this.jdbc=jdbc;this.payments=payments;this.audit=audit;this.transactions=new TransactionTemplate(transactionManager);}

    @Transactional
    public PaymentScheduleView create(FederatedIdentity identity,CreatePaymentScheduleRequest request,String correlationId){
        if(!"INR".equals(request.currency())||request.amountMinor()<=0)throw invalid("INVALID_SCHEDULE","Only positive INR schedules are supported.");
        if(!request.firstExecutionAt().isAfter(Instant.now()))throw invalid("INVALID_EXECUTION_TIME","The first execution must be in the future.");
        try{ZoneId.of(request.timezone());}catch(DateTimeException e){throw invalid("INVALID_TIMEZONE","Use a valid IANA timezone.");}
        long customer=customer(identity);Account source=ownedAccount(customer,request.sourceAccountId(),true);Destination destination=destination(customer,request.destinationType(),request.destinationId());
        if(source.id()==destination.accountId())throw invalid("SAME_ACCOUNT","Source and destination accounts must differ.");
        if(request.endAt()!=null&&!request.endAt().isAfter(request.firstExecutionAt()))throw invalid("INVALID_END_DATE","The end date must follow the first execution.");
        UUID id=UUID.randomUUID();
        jdbc.sql("""
          INSERT INTO payment_schedules(public_id,customer_id,source_account_id,destination_account_id,beneficiary_id,destination_type,amount_minor,currency,description,recurrence,timezone,next_execution_at,end_at,maximum_occurrences)
          VALUES(:public,:customer,:source,:destination,:beneficiary,CAST(:type AS payment_destination_type),:amount,'INR',:description,CAST(:recurrence AS payment_recurrence),:timezone,:next,:endAt,:maximum)
          """).param("public",id).param("customer",customer).param("source",source.id()).param("destination",destination.accountId()).param("beneficiary",destination.beneficiaryId(),java.sql.Types.BIGINT)
          .param("type",request.destinationType().name()).param("amount",request.amountMinor()).param("description",blank(request.description())).param("recurrence",request.recurrence().name()).param("timezone",request.timezone())
          .param("next",Timestamp.from(request.firstExecutionAt())).param("endAt",request.endAt()==null?null:Timestamp.from(request.endAt()),java.sql.Types.TIMESTAMP_WITH_TIMEZONE).param("maximum",request.maximumOccurrences(),java.sql.Types.INTEGER).update();
        audit.record(identity.subject(),"PAYMENT_SCHEDULE_CREATED","PAYMENT_SCHEDULE",id,"SUCCESS",correlationId,"{\"durable\":true}");return requireOwned(identity,id);
    }
    public List<PaymentScheduleView> list(FederatedIdentity i){return jdbc.sql(VIEW+" WHERE c.identity_issuer=:issuer AND c.identity_subject=:subject ORDER BY s.next_execution_at,s.id").param("issuer",i.issuer()).param("subject",i.subject()).query(this::map).list();}
    public PaymentScheduleView requireOwned(FederatedIdentity i,UUID id){return jdbc.sql(VIEW+" WHERE s.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject").param("id",id).param("issuer",i.issuer()).param("subject",i.subject()).query(this::map).optional().orElseThrow(this::notFound);}
    @Transactional public PaymentScheduleView cancel(FederatedIdentity i,UUID id,String correlationId){int changed=jdbc.sql("UPDATE payment_schedules s SET status='CANCELLED',updated_at=now(),lease_until=NULL FROM customers c WHERE s.customer_id=c.id AND s.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject AND s.status IN ('ACTIVE','PAUSED','REQUIRES_REVIEW')").param("id",id).param("issuer",i.issuer()).param("subject",i.subject()).update();if(changed!=1)throw new DomainException(HttpStatus.CONFLICT,"SCHEDULE_NOT_CANCELLABLE","The schedule is not cancellable.");audit.record(i.subject(),"PAYMENT_SCHEDULE_CANCELLED","PAYMENT_SCHEDULE",id,"SUCCESS",correlationId,"{}");return requireOwned(i,id);}

    @Scheduled(fixedDelayString="${novara.payments.scheduler-delay:30000}")
    public void executeDue(){var claimed=transactions.execute(status->claimDue(20));if(claimed==null)return;for(Long id:claimed){try{transactions.executeWithoutResult(status->executeClaimed(id));}catch(Exception e){log.error("Scheduled payment claim {} failed safely",id,e);transactions.executeWithoutResult(status->releaseForRetry(id));}}}
    @Transactional protected List<Long> claimDue(int limit){return jdbc.sql("""
      WITH due AS (SELECT id FROM payment_schedules WHERE status='ACTIVE' AND next_execution_at<=now() AND (lease_until IS NULL OR lease_until<now()) ORDER BY next_execution_at,id FOR UPDATE SKIP LOCKED LIMIT :limit)
      UPDATE payment_schedules s SET lease_until=now()+interval '2 minutes' FROM due WHERE s.id=due.id RETURNING s.id
      """).param("limit",limit).query(Long.class).list();}
    @Transactional protected void executeClaimed(long scheduleId){
        Schedule row=jdbc.sql("SELECT s.public_id,c.identity_issuer,c.identity_subject,sa.public_id,s.destination_type::text,CASE WHEN s.destination_type='BENEFICIARY' THEN b.public_id ELSE da.public_id END,s.amount_minor,s.currency,s.description,s.recurrence::text,s.timezone,s.next_execution_at,s.executed_occurrences,s.maximum_occurrences,s.end_at FROM payment_schedules s JOIN customers c ON c.id=s.customer_id JOIN bank_accounts sa ON sa.id=s.source_account_id JOIN bank_accounts da ON da.id=s.destination_account_id LEFT JOIN beneficiaries b ON b.id=s.beneficiary_id WHERE s.id=:id AND s.status='ACTIVE' AND s.lease_until>=now() FOR UPDATE OF s").param("id",scheduleId).query((rs,n)->new Schedule(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getObject(4,UUID.class),CreatePaymentIntentRequest.DestinationType.valueOf(rs.getString(5)),rs.getObject(6,UUID.class),rs.getLong(7),rs.getString(8),rs.getString(9),CreatePaymentScheduleRequest.Recurrence.valueOf(rs.getString(10)),rs.getString(11),rs.getTimestamp(12).toInstant(),rs.getInt(13),(Integer)rs.getObject(14),rs.getTimestamp(15)==null?null:rs.getTimestamp(15).toInstant())).optional().orElse(null);if(row==null)return;
        int occurrence=row.executed()+1;UUID occurrenceId=UUID.randomUUID();
        try{
            var identity=new FederatedIdentity(row.issuer(),row.subject());
            var intent=payments.createIntent(identity,new CreatePaymentIntentRequest(row.source(),row.type(),row.destination(),row.amount(),row.currency(),"INTERNAL",row.description()),"scheduler:"+row.id());
            jdbc.sql("INSERT INTO payment_schedule_occurrences(public_id,schedule_id,occurrence_number,scheduled_for,payment_intent_id,status) SELECT :public,:schedule,:number,:scheduled,p.id,'PENDING_AUTHORIZATION' FROM payment_intents p WHERE p.public_id=:payment")
              .param("public",occurrenceId).param("schedule",scheduleId).param("number",occurrence).param("scheduled",Timestamp.from(row.scheduled())).param("payment",intent.id()).update();
            if(intent.requiresStepUp()) {jdbc.sql("UPDATE payment_schedules SET status='REQUIRES_REVIEW',lease_until=NULL,updated_at=now() WHERE id=:id").param("id",scheduleId).update();jdbc.sql("UPDATE payment_schedule_occurrences SET status='REQUIRES_REVIEW',failure_code='STEP_UP_REQUIRED' WHERE public_id=:id").param("id",occurrenceId).update();return;}
            var completed=payments.confirm(identity,intent.id(),"schedule_"+occurrenceId.toString().replace("-",""),"scheduler:"+row.id());
            jdbc.sql("UPDATE payment_schedule_occurrences SET status='COMPLETED',completed_at=now() WHERE public_id=:id").param("id",occurrenceId).update();
            advance(scheduleId,row,occurrence);
        }catch(DomainException e){jdbc.sql("INSERT INTO payment_schedule_occurrences(public_id,schedule_id,occurrence_number,scheduled_for,status,failure_code) VALUES(:public,:schedule,:number,:scheduled,'FAILED',:code) ON CONFLICT(schedule_id,occurrence_number) DO UPDATE SET status='FAILED',failure_code=EXCLUDED.failure_code").param("public",occurrenceId).param("schedule",scheduleId).param("number",occurrence).param("scheduled",Timestamp.from(row.scheduled())).param("code",e.code()).update();jdbc.sql("UPDATE payment_schedules SET status='REQUIRES_REVIEW',lease_until=NULL,updated_at=now() WHERE id=:id").param("id",scheduleId).update();}
    }
    private void advance(long id,Schedule row,int occurrence){Instant next=next(row.scheduled(),row.recurrence(),ZoneId.of(row.timezone()));boolean done=row.recurrence()==CreatePaymentScheduleRequest.Recurrence.ONCE||(row.maximum()!=null&&occurrence>=row.maximum())||(row.end()!=null&&next.isAfter(row.end()));jdbc.sql("UPDATE payment_schedules SET executed_occurrences=:count,next_execution_at=:next,status=CAST(:status AS payment_schedule_status),lease_until=NULL,updated_at=now() WHERE id=:id").param("count",occurrence).param("next",Timestamp.from(next)).param("status",done?"COMPLETED":"ACTIVE").param("id",id).update();}
    private Instant next(Instant current,CreatePaymentScheduleRequest.Recurrence recurrence,ZoneId zone){ZonedDateTime z=current.atZone(zone);return switch(recurrence){case ONCE->z.plusYears(100).toInstant();case WEEKLY->z.plusWeeks(1).toInstant();case MONTHLY->{int day=z.getDayOfMonth();ZonedDateTime candidate=z.plusMonths(1);yield candidate.withDayOfMonth(Math.min(day,candidate.toLocalDate().lengthOfMonth())).toInstant();}};}
    @Transactional protected void releaseForRetry(long id){jdbc.sql("UPDATE payment_schedules SET lease_until=NULL WHERE id=:id AND status='ACTIVE'").param("id",id).update();}
    private long customer(FederatedIdentity i){return jdbc.sql("SELECT id FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE'").param("issuer",i.issuer()).param("subject",i.subject()).query(Long.class).optional().orElseThrow(this::notFound);}
    private Account ownedAccount(long c,UUID id,boolean debit){return jdbc.sql("SELECT a.id FROM bank_accounts a JOIN account_owners o ON o.account_id=a.id WHERE o.customer_id=:customer AND a.public_id=:id AND a.status='ACTIVE' AND a.debit_restricted=false AND (:debit=false OR o.can_debit=true)").param("customer",c).param("id",id).param("debit",debit).query((rs,n)->new Account(rs.getLong(1))).optional().orElseThrow(this::notFound);}
    private Destination destination(long c,CreatePaymentIntentRequest.DestinationType type,UUID id){if(type==CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT)return new Destination(ownedAccount(c,id,false).id(),null);return jdbc.sql("SELECT a.id,b.id FROM beneficiaries b JOIN bank_accounts a ON a.id=b.resolved_account_id WHERE b.customer_id=:customer AND b.public_id=:id AND b.status='ACTIVE' AND b.verified_at IS NOT NULL AND b.cooling_until<=now() AND b.transfer_method='INTERNAL' AND a.status='ACTIVE' AND a.credit_restricted=false").param("customer",c).param("id",id).query((rs,n)->new Destination(rs.getLong(1),rs.getLong(2))).optional().orElseThrow(()->invalid("BENEFICIARY_INELIGIBLE","The beneficiary cannot be scheduled."));}
    private PaymentScheduleView map(java.sql.ResultSet rs,int n)throws java.sql.SQLException{return new PaymentScheduleView(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),rs.getObject(5,UUID.class),rs.getString(6),rs.getString(7),rs.getString(8),rs.getLong(9),rs.getString(10),rs.getTimestamp(11).toInstant(),rs.getTimestamp(12)==null?null:rs.getTimestamp(12).toInstant(),(Integer)rs.getObject(13),rs.getInt(14),rs.getString(15));}
    private String blank(String s){return s==null||s.isBlank()?null:s.trim();}private DomainException notFound(){return new DomainException(HttpStatus.NOT_FOUND,"SCHEDULE_NOT_FOUND","Payment schedule was not found.");}private DomainException invalid(String c,String m){return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,c,m);}
    private record Account(long id){}private record Destination(long accountId,Long beneficiaryId){}private record Schedule(UUID id,String issuer,String subject,UUID source,CreatePaymentIntentRequest.DestinationType type,UUID destination,long amount,String currency,String description,CreatePaymentScheduleRequest.Recurrence recurrence,String timezone,Instant scheduled,int executed,Integer maximum,Instant end){}
    private static final String VIEW="""
      SELECT s.public_id,s.status::text,s.recurrence::text,s.timezone,sa.public_id,sa.masked_number,
        CASE WHEN s.destination_type='BENEFICIARY' THEN b.name ELSE da.nickname END,
        CASE WHEN s.destination_type='BENEFICIARY' THEN b.masked_account ELSE da.masked_number END,
        s.amount_minor,s.currency,s.next_execution_at,s.end_at,s.maximum_occurrences,s.executed_occurrences,s.description
      FROM payment_schedules s JOIN customers c ON c.id=s.customer_id JOIN bank_accounts sa ON sa.id=s.source_account_id
      JOIN bank_accounts da ON da.id=s.destination_account_id LEFT JOIN beneficiaries b ON b.id=s.beneficiary_id
      """;
}
