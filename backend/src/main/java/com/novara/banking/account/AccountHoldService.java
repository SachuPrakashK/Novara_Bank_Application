package com.novara.banking.account;

import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class AccountHoldService {
    private final JdbcClient jdbc;
    public AccountHoldService(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional
    public UUID place(String actor, UUID accountId, long amountMinor, String currency, String reason, String reference, Instant expiresAt) {
        if (amountMinor <= 0 || !"INR".equals(currency) || expiresAt == null || !expiresAt.isAfter(Instant.now()))
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_HOLD", "The hold instruction is invalid.");
        var internalId = jdbc.sql("""
            SELECT a.id FROM bank_accounts a JOIN account_owners o ON o.account_id=a.id
            JOIN customers c ON c.id=o.customer_id
            WHERE a.public_id=:account AND c.identity_subject=:actor AND c.status='ACTIVE'
            FOR UPDATE OF a
            """).param("account", accountId).param("actor", actor).query(Long.class).optional()
            .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "The account was not found."));
        var available = jdbc.sql("""
            SELECT b.balance_minor-a.minimum_balance_minor+a.overdraft_limit_minor-
              COALESCE((SELECT sum(h.amount_minor) FROM account_holds h WHERE h.account_id=a.id AND h.status='ACTIVE' AND h.expires_at>now()),0)
            FROM bank_accounts a JOIN ledger_balances b ON b.ledger_account_id=a.ledger_account_id WHERE a.id=:id
            """).param("id", internalId).query(Long.class).single();
        if (available < amountMinor) throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_AVAILABLE_FUNDS", "The account does not have sufficient available funds.");
        return jdbc.sql("""
            INSERT INTO account_holds(account_id,amount_minor,currency,reason,external_reference,expires_at)
            VALUES(:account,:amount,:currency,:reason,:reference,:expires) RETURNING public_id
            """).param("account", internalId).param("amount", amountMinor).param("currency", currency)
            .param("reason", reason).param("reference", reference).param("expires", Timestamp.from(expiresAt)).query(UUID.class).single();
    }

    @Transactional
    public void release(String actor, UUID holdId) {
        var changed = jdbc.sql("""
            UPDATE account_holds h SET status='RELEASED',released_at=now()
            FROM bank_accounts a JOIN account_owners o ON o.account_id=a.id JOIN customers c ON c.id=o.customer_id
            WHERE h.public_id=:hold AND h.account_id=a.id AND c.identity_subject=:actor AND h.status='ACTIVE'
            """).param("hold", holdId).param("actor", actor).update();
        if (changed != 1) throw new DomainException(HttpStatus.CONFLICT, "HOLD_NOT_RELEASABLE", "The hold cannot be released.");
    }
}
