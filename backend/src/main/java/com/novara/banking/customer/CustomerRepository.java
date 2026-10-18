package com.novara.banking.customer;

import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {
    private final JdbcClient jdbc;
    public CustomerRepository(JdbcClient jdbc) { this.jdbc = jdbc; }
    public CustomerProfile requireBySubject(String subject) {
        return jdbc.sql("SELECT public_id, display_name, status::text FROM customers WHERE identity_subject=:subject AND status='ACTIVE'")
            .param("subject", subject).query((rs, row) -> new CustomerProfile(rs.getObject(1, java.util.UUID.class), rs.getString(2), rs.getString(3))).optional()
            .orElseThrow(() -> new DomainException(HttpStatus.FORBIDDEN, "CUSTOMER_NOT_MAPPED", "The authenticated identity is not mapped to an active customer."));
    }
    public CustomerProfile requireByIdentity(String issuer, String subject) {
        return jdbc.sql("SELECT public_id, display_name, status::text FROM customers WHERE identity_issuer=:issuer AND identity_subject=:subject AND status='ACTIVE'")
            .param("issuer", issuer).param("subject", subject)
            .query((rs, row) -> new CustomerProfile(rs.getObject(1, java.util.UUID.class), rs.getString(2), rs.getString(3))).optional()
            .orElseThrow(() -> new DomainException(HttpStatus.FORBIDDEN, "CUSTOMER_NOT_MAPPED", "The authenticated identity is not mapped to an active customer."));
    }
}
