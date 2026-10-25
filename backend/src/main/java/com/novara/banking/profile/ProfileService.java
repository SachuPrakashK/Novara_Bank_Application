package com.novara.banking.profile;

import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity.FederatedIdentity;
import com.novara.banking.shared.DomainException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class ProfileService {
 private final JdbcClient jdbc; private final AuditService audit;
 public ProfileService(JdbcClient jdbc,AuditService audit){this.jdbc=jdbc;this.audit=audit;}
 public Profile view(FederatedIdentity i){return jdbc.sql("""
  SELECT c.display_name,c.preferred_display_name,c.registered_email,c.email_verified,c.registered_mobile,c.mobile_verified,c.postal_address,c.created_at,c.version,
   (SELECT count(*) FROM account_owners o WHERE o.customer_id=c.id)
  FROM customers c WHERE c.identity_issuer=:issuer AND c.identity_subject=:subject AND c.status='ACTIVE'
  """).param("issuer",i.issuer()).param("subject",i.subject()).query((r,n)->new Profile(r.getString(1),r.getString(2),r.getString(3),r.getBoolean(4),r.getString(5),r.getBoolean(6),r.getString(7),r.getTimestamp(8).toInstant(),r.getLong(9),r.getInt(10))).optional().orElseThrow(this::missing);}
 @Transactional public Profile update(FederatedIdentity i,ProfileUpdate request){int changed=jdbc.sql("UPDATE customers SET preferred_display_name=:name,version=version+1,updated_at=now() WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE' AND version=:version").param("name",request.preferredDisplayName()).param("issuer",i.issuer()).param("subject",i.subject()).param("version",request.version()).update();if(changed!=1)throw new DomainException(HttpStatus.CONFLICT,"PROFILE_CHANGED","The profile changed. Refresh and try again.");audit.record(i.subject(),"PROFILE_UPDATED","CUSTOMER",null,"SUCCESS",MDC.get("correlationId"),"{}");return view(i);}
 public Preferences preferences(FederatedIdentity i){ensurePreferences(i);return jdbc.sql("SELECT p.language,p.theme,p.date_format,p.reduced_motion,p.version FROM customer_preferences p JOIN customers c ON c.id=p.customer_id WHERE c.identity_issuer=:issuer AND c.identity_subject=:subject").param("issuer",i.issuer()).param("subject",i.subject()).query((r,n)->new Preferences(r.getString(1),r.getString(2),r.getString(3),r.getBoolean(4),r.getLong(5))).single();}
 @Transactional public Preferences updatePreferences(FederatedIdentity i,Preferences request){ensurePreferences(i);int changed=jdbc.sql("UPDATE customer_preferences p SET language=:language,theme=:theme,date_format=:format,reduced_motion=:motion,version=version+1,updated_at=now() FROM customers c WHERE p.customer_id=c.id AND c.identity_issuer=:issuer AND c.identity_subject=:subject AND p.version=:version").param("language",request.language()).param("theme",request.theme()).param("format",request.dateFormat()).param("motion",request.reducedMotion()).param("issuer",i.issuer()).param("subject",i.subject()).param("version",request.version()).update();if(changed!=1)throw new DomainException(HttpStatus.CONFLICT,"PREFERENCES_CHANGED","Preferences changed. Refresh and try again.");audit.record(i.subject(),"PREFERENCES_UPDATED","CUSTOMER",null,"SUCCESS",MDC.get("correlationId"),"{}");return preferences(i);}
 private void ensurePreferences(FederatedIdentity i){jdbc.sql("INSERT INTO customer_preferences(customer_id) SELECT id FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject ON CONFLICT DO NOTHING").param("issuer",i.issuer()).param("subject",i.subject()).update();}
 private DomainException missing(){return new DomainException(HttpStatus.NOT_FOUND,"PROFILE_NOT_FOUND","The profile was not found.");}
 public record Profile(String fullName,String preferredDisplayName,String registeredEmail,boolean emailVerified,String registeredMobile,boolean mobileVerified,String address,Instant customerSince,long version,int linkedAccountCount){}
 public record ProfileUpdate(@jakarta.validation.constraints.Size(max=80) @jakarta.validation.constraints.Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]{0,79}$") String preferredDisplayName,long version){}
 public record Preferences(@jakarta.validation.constraints.Pattern(regexp="en") String language,@jakarta.validation.constraints.Pattern(regexp="SYSTEM|LIGHT|DARK") String theme,@jakarta.validation.constraints.Pattern(regexp="DD_MMM_YYYY|DD_MM_YYYY") String dateFormat,boolean reducedMotion,long version){}
}

