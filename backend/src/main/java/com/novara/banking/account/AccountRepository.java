package com.novara.banking.account;

import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public class AccountRepository {
    private static final String OWNED_ACCOUNTS = """
        SELECT a.public_id,a.nickname,a.type::text,a.masked_number,a.currency,a.status::text,
          b.balance_minor-a.minimum_balance_minor+a.overdraft_limit_minor-
            COALESCE((SELECT sum(h.amount_minor) FROM account_holds h WHERE h.account_id=a.id AND h.status='ACTIVE' AND h.expires_at>now()),0),
          b.balance_minor
        FROM bank_accounts a JOIN account_owners o ON o.account_id=a.id JOIN customers c ON c.id=o.customer_id
        JOIN ledger_balances b ON b.ledger_account_id=a.ledger_account_id WHERE c.identity_subject=:subject
        """;
    private final JdbcClient jdbc;
    public AccountRepository(JdbcClient jdbc) { this.jdbc = jdbc; }
    public List<AccountView> findOwned(String subject) { return jdbc.sql(OWNED_ACCOUNTS + " ORDER BY a.created_at").param("subject",subject).query(this::map).list(); }
    public AccountView requireOwned(String subject, UUID accountId) {
        return jdbc.sql(OWNED_ACCOUNTS + " AND a.public_id=:id").param("subject",subject).param("id",accountId).query(this::map).optional()
            .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND,"ACCOUNT_NOT_FOUND","The account was not found."));
    }
    public List<AccountTransactionView> transactions(String subject, UUID accountId, int limit) {
        requireOwned(subject, accountId);
        return jdbc.sql("""
            SELECT j.public_id,j.journal_reference,j.posted_at,j.description,l.direction::text,l.amount_minor,l.currency
            FROM bank_accounts a JOIN journal_lines l ON l.ledger_account_id=a.ledger_account_id JOIN journals j ON j.id=l.journal_id
            WHERE a.public_id=:id AND j.status IN ('POSTED','REVERSED') ORDER BY j.posted_at DESC,l.id DESC LIMIT :limit
            """).param("id",accountId).param("limit",limit).query((rs,row)->new AccountTransactionView(rs.getObject(1,UUID.class),rs.getString(2),rs.getTimestamp(3).toInstant(),rs.getString(4),rs.getString(5),rs.getLong(6),rs.getString(7))).list();
    }
    public List<CustomerTransactionView> transactions(String subject, int limit) {
        return jdbc.sql("""
            SELECT j.public_id,j.description,j.posted_at,a.masked_number,l.amount_minor,l.currency,l.direction::text,j.journal_reference
            FROM customers c JOIN account_owners o ON o.customer_id=c.id JOIN bank_accounts a ON a.id=o.account_id
            JOIN journal_lines l ON l.ledger_account_id=a.ledger_account_id JOIN journals j ON j.id=l.journal_id
            WHERE c.identity_subject=:subject AND j.status IN ('POSTED','REVERSED')
            ORDER BY j.posted_at DESC,l.id DESC LIMIT :limit
            """).param("subject",subject).param("limit",limit).query((rs,row)->new CustomerTransactionView(
                rs.getObject(1,UUID.class),rs.getString(2),rs.getTimestamp(3).toInstant(),rs.getString(4),
                new CustomerTransactionView.Money(rs.getLong(5),rs.getString(6)),rs.getString(7),"COMPLETED",rs.getString(8),"Transfer",null)).list();
    }
    private AccountView map(java.sql.ResultSet rs, int row) throws java.sql.SQLException { return new AccountView(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getLong(7),rs.getLong(8)); }
}
