package com.novara.banking.beneficiary;

import com.novara.banking.audit.AuditService;
import com.novara.banking.shared.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BeneficiaryService {
    private static final String CREATE_OPERATION = "BENEFICIARY_CREATE";
    private final JdbcClient jdbc;
    private final AuditService audit;
    private final byte[] hashKey;
    private final Duration coolingPeriod;

    public BeneficiaryService(JdbcClient jdbc, AuditService audit,
                              @Value("${novara.beneficiaries.hash-key}") String hashKey,
                              @Value("${novara.beneficiaries.cooling-period:24h}") Duration coolingPeriod) {
        if (hashKey.length() < 32) throw new IllegalArgumentException("Beneficiary hash key must contain at least 32 characters");
        this.jdbc = jdbc; this.audit = audit; this.hashKey = hashKey.getBytes(StandardCharsets.UTF_8); this.coolingPeriod = coolingPeriod;
    }

    public List<BeneficiaryView> list(String issuer, String subject, String query, String status, Boolean favorite) {
        String safeQuery = query == null ? "" : query.trim();
        String safeStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return jdbc.sql(VIEW_SQL + " WHERE c.identity_issuer=:issuer AND c.identity_subject=:subject" +
                " AND (:query='' OR lower(b.name) LIKE lower('%'||:query||'%') OR lower(COALESCE(b.nickname,'')) LIKE lower('%'||:query||'%') OR lower(b.bank_name) LIKE lower('%'||:query||'%'))" +
                " AND (:status='' OR b.status::text=:status) AND (:filterFavorite=false OR b.favorite=:favorite)" +
                " ORDER BY b.favorite DESC,b.updated_at DESC,b.public_id")
            .param("issuer", issuer).param("subject", subject).param("query", safeQuery).param("status", safeStatus)
            .param("filterFavorite", favorite != null).param("favorite", Boolean.TRUE.equals(favorite))
            .query(this::map).list();
    }

    public BeneficiaryView requireOwned(String issuer, String subject, UUID id) {
        return jdbc.sql(VIEW_SQL + " WHERE b.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject")
            .param("id", id).param("issuer", issuer).param("subject", subject).query(this::map).optional()
            .orElseThrow(this::notFound);
    }

    @Transactional
    public BeneficiaryView create(String issuer, String subject, String key, CreateBeneficiaryRequest request, String correlationId) {
        validateKey(key);
        String actor = subject;
        String normalizedRouting = request.routingCode().trim().toUpperCase(Locale.ROOT);
        String normalizedAccount = request.accountNumber().replaceAll("\\s", "");
        String destinationHash = hmac(normalizedRouting + "|" + normalizedAccount);
        String requestHash = hmac(request.name().trim() + "|" + nullToEmpty(request.nickname()) + "|" + request.bankName().trim() + "|" + request.transferMethod() + "|" + destinationHash);
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:key,0))").param("key", actor + "|" + CREATE_OPERATION + "|" + key).query(Integer.class).single();
        var existing = jdbc.sql("SELECT request_hash,resource_public_id FROM idempotency_records WHERE principal_subject=:actor AND operation_type=:operation AND idempotency_key=:key")
            .param("actor", actor).param("operation", CREATE_OPERATION).param("key", key)
            .query((rs,row) -> new ExistingRequest(rs.getString(1), rs.getObject(2, UUID.class))).optional();
        if (existing.isPresent()) {
            if (!existing.get().requestHash().equals(requestHash)) throw new DomainException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", "The idempotency key was already used for different beneficiary details.");
            return requireOwned(issuer, subject, existing.get().resourceId());
        }
        long customerId = requireCustomerId(issuer, subject);
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:key,0))").param("key", "beneficiary|" + customerId + "|" + destinationHash + "|" + normalizedRouting).query(Integer.class).single();
        boolean duplicate = jdbc.sql("SELECT EXISTS(SELECT 1 FROM beneficiaries WHERE customer_id=:customer AND account_reference_hash=:hash AND routing_code=:routing AND status<>'DEACTIVATED')")
            .param("customer", customerId).param("hash", destinationHash).param("routing", normalizedRouting).query(Boolean.class).single();
        if (duplicate) throw new DomainException(HttpStatus.CONFLICT, "BENEFICIARY_ALREADY_EXISTS", "This beneficiary destination already exists.");
        UUID publicId = UUID.randomUUID();
        long internalId = jdbc.sql("""
            INSERT INTO beneficiaries(public_id,customer_id,name,nickname,bank_name,account_reference_hash,masked_account,routing_code,transfer_method)
            VALUES(:id,:customer,:name,:nickname,:bank,:hash,:masked,:routing,:method) RETURNING id
            """).param("id", publicId).param("customer", customerId).param("name", request.name().trim())
            .param("nickname", blankToNull(request.nickname())).param("bank", request.bankName().trim()).param("hash", destinationHash)
            .param("masked", mask(normalizedAccount)).param("routing", normalizedRouting).param("method", request.transferMethod()).query(Long.class).single();
        jdbc.sql("INSERT INTO beneficiary_verifications(beneficiary_id,status) VALUES(:id,'PENDING')").param("id", internalId).update();
        jdbc.sql("INSERT INTO idempotency_records(principal_subject,operation_type,idempotency_key,request_hash,resource_type,resource_public_id,response_status,expires_at) VALUES(:actor,:operation,:key,:hash,'BENEFICIARY',:resource,201,now()+interval '24 hours')")
            .param("actor", actor).param("operation", CREATE_OPERATION).param("key", key).param("hash", requestHash).param("resource", publicId).update();
        audit.record(actor, "BENEFICIARY_CREATED", "BENEFICIARY", publicId, "SUCCESS", correlationId, "{\"ownershipVerified\":true,\"verificationStatus\":\"PENDING\"}");
        return requireOwned(issuer, subject, publicId);
    }

    @Transactional
    public BeneficiaryView updateProfile(String issuer, String subject, UUID id, UpdateBeneficiaryRequest request, String correlationId) {
        int changed = jdbc.sql("""
            UPDATE beneficiaries b SET name=:name,nickname=:nickname,updated_at=now(),version=version+1
            FROM customers c WHERE b.customer_id=c.id AND b.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject AND b.status<>'DEACTIVATED'
            """).param("name", request.name().trim()).param("nickname", blankToNull(request.nickname())).param("id", id).param("issuer", issuer).param("subject", subject).update();
        if (changed != 1) throw notFound();
        audit.record(subject, "BENEFICIARY_PROFILE_UPDATED", "BENEFICIARY", id, "SUCCESS", correlationId, "{\"ownershipVerified\":true}");
        return requireOwned(issuer, subject, id);
    }

    @Transactional
    public BeneficiaryView setFavorite(String issuer, String subject, UUID id, boolean favorite, String correlationId) {
        int changed = jdbc.sql("""
            UPDATE beneficiaries b SET favorite=:favorite,updated_at=now(),version=version+1 FROM customers c
            WHERE b.customer_id=c.id AND b.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject AND b.status<>'DEACTIVATED'
            """).param("favorite", favorite).param("id", id).param("issuer", issuer).param("subject", subject).update();
        if (changed != 1) throw notFound();
        audit.record(subject, favorite ? "BENEFICIARY_FAVORITED" : "BENEFICIARY_UNFAVORITED", "BENEFICIARY", id, "SUCCESS", correlationId, "{\"ownershipVerified\":true}");
        return requireOwned(issuer, subject, id);
    }

    @Transactional
    public void deactivate(String issuer, String subject, UUID id, String correlationId) {
        int changed = jdbc.sql("""
            UPDATE beneficiaries b SET status='DEACTIVATED',favorite=false,deactivated_at=now(),updated_at=now(),version=version+1 FROM customers c
            WHERE b.customer_id=c.id AND b.public_id=:id AND c.identity_issuer=:issuer AND c.identity_subject=:subject AND b.status<>'DEACTIVATED'
            """).param("id", id).param("issuer", issuer).param("subject", subject).update();
        if (changed != 1) throw notFound();
        audit.record(subject, "BENEFICIARY_DEACTIVATED", "BENEFICIARY", id, "SUCCESS", correlationId, "{\"ownershipVerified\":true,\"stepUpVerified\":true}");
    }

    @Transactional
    public BeneficiaryView completeVerification(UUID id, boolean verified, String providerReference) {
        return completeVerification(id, verified, providerReference, null);
    }

    /** Trusted verification boundary. A verified internal destination is supplied as an opaque account UUID by the provider. */
    @Transactional
    public BeneficiaryView completeVerification(UUID id, boolean verified, String providerReference, UUID resolvedAccountId) {
        long internalId = jdbc.sql("SELECT id FROM beneficiaries WHERE public_id=:id AND status='PENDING_VERIFICATION' FOR UPDATE")
            .param("id", id).query(Long.class).optional().orElseThrow(this::notFound);
        Long resolvedInternalId = null;
        if (verified && resolvedAccountId != null) {
            resolvedInternalId = jdbc.sql("SELECT id FROM bank_accounts WHERE public_id=:id AND status='ACTIVE' AND credit_restricted=false")
                .param("id", resolvedAccountId).query(Long.class).optional()
                .orElseThrow(() -> new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"DESTINATION_UNRESOLVABLE","The verified destination is not an eligible Novara account."));
        }
        jdbc.sql("UPDATE beneficiary_verifications SET status=:status,provider_reference=:reference,completed_at=now() WHERE beneficiary_id=:id AND status='PENDING'")
            .param("status", verified ? "VERIFIED" : "FAILED").param("reference", providerReference).param("id", internalId).update();
        Instant availableAt = verified ? Instant.now().plus(coolingPeriod) : Instant.EPOCH;
        jdbc.sql("UPDATE beneficiaries SET status=CAST(:status AS beneficiary_status),verified_at=CASE WHEN :verified THEN now() ELSE NULL END,cooling_until=CASE WHEN :verified THEN CAST(:until AS timestamptz) ELSE NULL END,resolved_account_id=:resolved,updated_at=now(),version=version+1 WHERE id=:id")
            .param("status", verified ? "ACTIVE" : "RESTRICTED").param("verified", verified).param("until", Timestamp.from(availableAt))
            .param("resolved", resolvedInternalId, java.sql.Types.BIGINT).param("id", internalId).update();
        audit.record("BENEFICIARY_VERIFICATION_PROVIDER", verified ? "BENEFICIARY_VERIFIED" : "BENEFICIARY_VERIFICATION_FAILED", "BENEFICIARY", id,
            verified ? "SUCCESS" : "REJECTED", null, "{\"providerResultRecorded\":true}");
        return jdbc.sql(VIEW_SQL + " WHERE b.id=:id").param("id", internalId).query(this::map).single();
    }

    private long requireCustomerId(String issuer, String subject) {
        return jdbc.sql("SELECT id FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE'")
            .param("issuer", issuer).param("subject", subject).query(Long.class).optional()
            .orElseThrow(() -> new DomainException(HttpStatus.FORBIDDEN, "CUSTOMER_NOT_MAPPED", "The authenticated identity is not mapped to an active customer."));
    }
    private BeneficiaryView map(ResultSet rs, int row) throws SQLException {
        var available = rs.getTimestamp(11);
        return new BeneficiaryView(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),rs.getBoolean(10),available==null?null:available.toInstant(),rs.getTimestamp(12).toInstant(),rs.getTimestamp(13).toInstant());
    }
    private void validateKey(String key) { if (key == null || !key.matches("[A-Za-z0-9_-]{16,100}")) throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","A valid Idempotency-Key header is required."); }
    private String hmac(String value) { try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(hashKey,"HmacSHA256")); return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception impossible) { throw new IllegalStateException("HMAC is unavailable", impossible); } }
    private String mask(String account) { return "•••• " + account.substring(account.length()-4); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String nullToEmpty(String value) { return value == null ? "" : value.trim(); }
    private DomainException notFound() { return new DomainException(HttpStatus.NOT_FOUND,"BENEFICIARY_NOT_FOUND","Beneficiary was not found."); }
    private record ExistingRequest(String requestHash, UUID resourceId) {}
    private static final String VIEW_SQL = """
        SELECT b.public_id,b.name,b.nickname,COALESCE(b.nickname,b.name),b.bank_name,b.masked_account,b.routing_code,b.transfer_method,b.status::text,b.favorite,
          CASE WHEN b.status='ACTIVE' THEN b.cooling_until ELSE NULL END,b.created_at,b.updated_at
        FROM beneficiaries b JOIN customers c ON c.id=b.customer_id
        """;
}

