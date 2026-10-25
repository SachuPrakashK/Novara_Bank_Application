package com.novara.banking.loan;

import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity.FederatedIdentity;
import com.novara.banking.loan.LoanModels.*;
import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.*;
import java.util.*;

@Service public class LoanService {
 private static final String PRODUCT_SQL="""
  SELECT p.id,p.public_id,p.code,p.name,p.category,p.currency,p.minimum_principal_minor,p.maximum_principal_minor,
   p.minimum_tenure_months,p.maximum_tenure_months,v.id,v.annual_rate_basis_points,v.policy_version,v.calculation_method,v.processing_fee_minor
  FROM loan_products p JOIN loan_product_versions v ON v.product_id=p.id
  WHERE p.status='ACTIVE' AND CURRENT_DATE BETWEEN p.effective_from AND COALESCE(p.effective_to,CURRENT_DATE)
   AND CURRENT_DATE BETWEEN v.effective_from AND COALESCE(v.effective_to,CURRENT_DATE)
  """;
 private final JdbcClient jdbc; private final LoanEmiCalculator calculator; private final AuditService audit;
 public LoanService(JdbcClient jdbc,LoanEmiCalculator calculator,AuditService audit){this.jdbc=jdbc;this.calculator=calculator;this.audit=audit;}

 public List<Product> products(){return jdbc.sql(PRODUCT_SQL+" ORDER BY p.category,p.name").query((r,n)->toProduct(productRow(r))).list();}

 @Transactional public Quote quote(FederatedIdentity identity,QuoteRequest request){
  long customer=customer(identity); ProductRow p=product(request.productId()); validate(p,request.principalMinor(),request.tenureMonths());
  LocalDate firstDue=LocalDate.now().plusMonths(1); var calculated=calculator.calculate(request.principalMinor(),p.rate,request.tenureMonths(),firstDue);
  UUID publicId=UUID.randomUUID(); Instant expires=Instant.now().plus(Duration.ofMinutes(30)); LocalDate maturity=firstDue.plusMonths(request.tenureMonths()-1L);
  long quoteId=jdbc.sql("""
   INSERT INTO loan_quotes(public_id,customer_id,product_id,product_version_id,principal_minor,currency,tenure_months,annual_rate_basis_points,emi_minor,total_interest_minor,total_repayment_minor,processing_fee_minor,first_installment_date,maturity_date,expires_at)
   VALUES(:public,:customer,:product,:version,:principal,:currency,:tenure,:rate,:emi,:interest,:repayment,:fee,:first,:maturity,:expires) RETURNING id
   """).param("public",publicId).param("customer",customer).param("product",p.id).param("version",p.versionId).param("principal",request.principalMinor())
   .param("currency",p.currency).param("tenure",request.tenureMonths()).param("rate",p.rate).param("emi",calculated.emiMinor())
   .param("interest",calculated.totalInterestMinor()).param("repayment",calculated.totalRepaymentMinor()).param("fee",p.fee)
   .param("first",firstDue).param("maturity",maturity).param("expires",Timestamp.from(expires)).query(Long.class).single();
  for(Installment i:calculated.schedule()) jdbc.sql("""
   INSERT INTO loan_quote_installments(quote_id,sequence_number,due_date,opening_principal_minor,scheduled_principal_minor,scheduled_interest_minor,scheduled_fee_minor,total_due_minor,closing_principal_minor)
   VALUES(:quote,:sequence,:due,:opening,:principal,:interest,:fee,:total,:closing)
   """).param("quote",quoteId).param("sequence",i.sequence()).param("due",i.dueDate()).param("opening",i.openingPrincipalMinor())
   .param("principal",i.principalMinor()).param("interest",i.interestMinor()).param("fee",i.feeMinor()).param("total",i.totalDueMinor()).param("closing",i.closingPrincipalMinor()).update();
  audit.record(identity.subject(),"LOAN_QUOTE_CREATED","LOAN_QUOTE",publicId,"SUCCESS",null,"{\"sandbox\":true,\"nonBinding\":true}");
  return new Quote(publicId,p.publicId,p.name,p.policyVersion,request.principalMinor(),p.currency,request.tenureMonths(),p.rate,calculated.emiMinor(),
   calculated.totalInterestMinor(),calculated.totalRepaymentMinor(),p.fee,firstDue,maturity,expires,calculated.schedule(),
   "Fictional sandbox quote; fixed monthly periodic rate, HALF_EVEN minor-unit rounding, no fees, no holiday adjustment, and final-installment principal reconciliation.");
 }

 @Transactional public Application apply(FederatedIdentity identity,ApplicationRequest request,String key,String correlation){
  validateKey(key); long customer=customer(identity); String operation="LOAN_APPLICATION",requestHash=hash(request);
  jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:key,0))").param("key",identity.subject()+'|'+operation+'|'+key).query(Integer.class).single();
  var prior=jdbc.sql("SELECT request_hash,resource_public_id FROM idempotency_records WHERE principal_subject=:subject AND operation_type=:operation AND idempotency_key=:key")
   .param("subject",identity.subject()).param("operation",operation).param("key",key).query((r,n)->new Prior(r.getString(1),r.getObject(2,UUID.class))).optional();
  if(prior.isPresent()){if(!prior.get().hash.equals(requestHash))throw conflict("IDEMPOTENCY_KEY_REUSED","The key was used for another loan application.");return requireApplication(identity,prior.get().resource);}
  QuoteRow quote=ownedQuote(customer,request.quoteId(),true); UUID id=UUID.randomUUID(); String reference="LAP-"+id.toString().substring(0,8).toUpperCase(Locale.ROOT);
  jdbc.sql("INSERT INTO idempotency_records(principal_subject,operation_type,idempotency_key,request_hash,resource_type,resource_public_id,expires_at) VALUES(:subject,:operation,:key,:hash,'LOAN_APPLICATION',:resource,now()+interval '7 days')")
   .param("subject",identity.subject()).param("operation",operation).param("key",key).param("hash",requestHash).param("resource",id).update();
  long internal=jdbc.sql("""
   INSERT INTO loan_applications(public_id,reference,customer_id,product_id,quote_id,status,purpose_category,customer_declaration,idempotency_key,request_hash)
   VALUES(:public,:reference,:customer,:product,:quote,'SUBMITTED',:purpose,true,:key,:hash) RETURNING id
   """).param("public",id).param("reference",reference).param("customer",customer).param("product",quote.productId).param("quote",quote.id)
   .param("purpose",request.purposeCategory()).param("key",key).param("hash",requestHash).query(Long.class).single();
  jdbc.sql("INSERT INTO loan_status_events(application_id,new_status,reason_code) VALUES(:id,'SUBMITTED','CUSTOMER_SUBMITTED')").param("id",internal).update();
  jdbc.sql("UPDATE idempotency_records SET response_status=201 WHERE principal_subject=:subject AND operation_type=:operation AND idempotency_key=:key")
   .param("subject",identity.subject()).param("operation",operation).param("key",key).update();
  audit.record(identity.subject(),"LOAN_APPLICATION_SUBMITTED","LOAN_APPLICATION",id,"SUCCESS",correlation,"{\"sandbox\":true,\"creditDecisionMade\":false}");
  return requireApplication(identity,id);
 }

 public List<Application> applications(FederatedIdentity identity){return jdbc.sql(APP_VIEW+" WHERE a.customer_id=:customer ORDER BY a.submitted_at DESC").param("customer",customer(identity)).query(this::mapApplication).list();}
 public Application requireApplication(FederatedIdentity identity,UUID id){return jdbc.sql(APP_VIEW+" WHERE a.customer_id=:customer AND a.public_id=:id").param("customer",customer(identity)).param("id",id).query(this::mapApplication).optional().orElseThrow(this::notFound);}
 public List<Installment> quoteSchedule(FederatedIdentity identity,UUID quoteId){return jdbc.sql("""
  SELECT i.sequence_number,i.due_date,i.opening_principal_minor,i.scheduled_principal_minor,i.scheduled_interest_minor,i.scheduled_fee_minor,i.total_due_minor,i.closing_principal_minor
  FROM loan_quote_installments i JOIN loan_quotes q ON q.id=i.quote_id WHERE q.customer_id=:customer AND q.public_id=:quote ORDER BY i.sequence_number
  """).param("customer",customer(identity)).param("quote",quoteId).query((r,n)->new Installment(r.getInt(1),r.getObject(2,LocalDate.class),r.getLong(3),r.getLong(4),r.getLong(5),r.getLong(6),r.getLong(7),r.getLong(8),"ILLUSTRATIVE")).list();}

 private ProductRow product(UUID id){return jdbc.sql(PRODUCT_SQL+" AND p.public_id=:id").param("id",id).query((r,n)->productRow(r)).optional().orElseThrow(()->new DomainException(HttpStatus.NOT_FOUND,"LOAN_PRODUCT_NOT_FOUND","The sandbox loan product is unavailable."));}
 private ProductRow productRow(ResultSet r)throws SQLException{return new ProductRow(r.getLong(1),r.getObject(2,UUID.class),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getLong(7),r.getLong(8),r.getInt(9),r.getInt(10),r.getLong(11),r.getInt(12),r.getString(13),r.getString(14),r.getLong(15));}
 private Product toProduct(ProductRow p){return new Product(p.publicId,p.code,p.name,p.category,p.currency,p.min,p.max,p.minMonths,p.maxMonths,p.rate,p.policyVersion,p.method,p.fee,true,false,false);}
 private void validate(ProductRow p,long principal,int tenure){if(principal<p.min||principal>p.max)throw invalid("LOAN_PRINCIPAL_OUT_OF_RANGE","The requested principal is outside this product's sandbox limits.");if(tenure<p.minMonths||tenure>p.maxMonths)throw invalid("LOAN_TENURE_OUT_OF_RANGE","The requested tenure is outside this product's sandbox limits.");}
 private QuoteRow ownedQuote(long customer,UUID id,boolean lock){String suffix=lock?" FOR UPDATE OF q":"";return jdbc.sql("SELECT q.id,q.product_id FROM loan_quotes q WHERE q.customer_id=:customer AND q.public_id=:id AND q.expires_at>now() AND NOT EXISTS(SELECT 1 FROM loan_applications a WHERE a.quote_id=q.id)"+suffix).param("customer",customer).param("id",id).query((r,n)->new QuoteRow(r.getLong(1),r.getLong(2))).optional().orElseThrow(()->invalid("LOAN_QUOTE_UNAVAILABLE","The quote is expired, already used, or unavailable."));}
 private long customer(FederatedIdentity i){return jdbc.sql("SELECT id FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE'").param("issuer",i.issuer()).param("subject",i.subject()).query(Long.class).optional().orElseThrow(this::notFound);}
 private Application mapApplication(ResultSet r,int n)throws SQLException{return new Application(r.getObject(1,UUID.class),r.getString(2),r.getObject(3,UUID.class),r.getString(4),r.getString(5),r.getString(6),r.getLong(7),r.getString(8),r.getInt(9),r.getInt(10),r.getLong(11),r.getTimestamp(12).toInstant(),"Submitted for fictional sandbox review. No approval, booking, or disbursement has occurred.",false);}
 private String hash(ApplicationRequest r){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((r.quoteId()+"|"+r.purposeCategory()+"|"+r.customerDeclaration()).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 private void validateKey(String k){if(k==null||!k.matches("[A-Za-z0-9_-]{16,100}"))throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","A valid Idempotency-Key is required.");}
 private DomainException invalid(String c,String m){return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,c,m);} private DomainException conflict(String c,String m){return new DomainException(HttpStatus.CONFLICT,c,m);} private DomainException notFound(){return new DomainException(HttpStatus.NOT_FOUND,"LOAN_RESOURCE_NOT_FOUND","The loan resource was not found.");}
 private record ProductRow(long id,UUID publicId,String code,String name,String category,String currency,long min,long max,int minMonths,int maxMonths,long versionId,int rate,String policyVersion,String method,long fee){} private record QuoteRow(long id,long productId){} private record Prior(String hash,UUID resource){}
 private static final String APP_VIEW="""
  SELECT a.public_id,a.reference,q.public_id,p.name,p.category,a.status,q.principal_minor,q.currency,q.tenure_months,q.annual_rate_basis_points,q.emi_minor,a.submitted_at
  FROM loan_applications a JOIN loan_quotes q ON q.id=a.quote_id JOIN loan_products p ON p.id=a.product_id
  """;
}
