package com.novara.banking.deposit;

import com.novara.banking.ledger.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class DepositInstallmentService {
    private final JdbcClient jdbc; private final LedgerPostingService ledger;
    public DepositInstallmentService(JdbcClient jdbc,LedgerPostingService ledger){this.jdbc=jdbc;this.ledger=ledger;}
    @Scheduled(fixedDelayString="${novara.deposits.installment-worker-delay:PT1M}",initialDelayString="${novara.deposits.installment-worker-initial-delay:PT1M}")
    public void runDueInstallments(){for(int i=0;i<25&&processOne();i++);}
    @Transactional public boolean processOne(){
        var due=jdbc.sql("""
          SELECT o.id,o.deposit_account_id,o.sequence_number,o.amount_minor,o.currency,o.idempotency_key,d.reference,d.status,d.ledger_account_id,
           p.grace_period_days,p.missed_installment_policy,f.ledger_account_id,
           b.balance_minor-f.minimum_balance_minor+f.overdraft_limit_minor-COALESCE((SELECT sum(h.amount_minor) FROM account_holds h WHERE h.account_id=f.id AND h.status='ACTIVE' AND h.expires_at>now()),0),
           f.status::text,f.debit_restricted,o.scheduled_date
          FROM deposit_installment_occurrences o JOIN deposit_accounts d ON d.id=o.deposit_account_id JOIN deposit_products p ON p.id=d.product_id
          JOIN bank_accounts f ON f.id=d.funding_account_id JOIN ledger_balances b ON b.ledger_account_id=f.ledger_account_id
          WHERE o.status='SCHEDULED' AND o.scheduled_date<=CURRENT_DATE AND (o.claimed_at IS NULL OR o.claimed_at<now()-interval '15 minutes') ORDER BY o.scheduled_date,o.id FOR UPDATE OF o,d,f,b SKIP LOCKED LIMIT 1
          """).query((r,n)->new Due(r.getLong(1),r.getLong(2),r.getInt(3),r.getLong(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8),r.getLong(9),r.getInt(10),r.getString(11),r.getLong(12),r.getLong(13),r.getString(14),r.getBoolean(15),r.getObject(16,LocalDate.class))).optional();
        if(due.isEmpty())return false; Due d=due.get();
        jdbc.sql("UPDATE deposit_installment_occurrences SET status='PROCESSING',attempt_count=attempt_count+1,claimed_at=now(),updated_at=now() WHERE id=:id").param("id",d.occurrenceId).update();
        boolean retry=!LocalDate.now().isAfter(d.scheduledDate.plusDays(d.graceDays))&&"RETRY_DURING_GRACE".equals(d.policy);
        if(!"ACTIVE".equals(d.depositStatus)){fail(d,"DEPOSIT_NOT_ACTIVE",false);return true;}
        if(!"ACTIVE".equals(d.fundingStatus)||d.debitRestricted){fail(d,"FUNDING_ACCOUNT_RESTRICTED",retry);return true;}
        if(d.availableMinor<d.amount){fail(d,"INSUFFICIENT_FUNDS",retry);return true;}
        String journalRef=d.reference+"-INST-"+d.sequence;
        var posting=ledger.post(new JournalCommand("BATCH-"+journalRef,journalRef,d.currency,"Recurring deposit installment "+d.sequence,List.of(new JournalLineCommand(d.fundingLedger,JournalLineCommand.Direction.DEBIT,d.amount,d.currency),new JournalLineCommand(d.depositLedger,JournalLineCommand.Direction.CREDIT,d.amount,d.currency)),null));
        jdbc.sql("UPDATE deposit_installment_occurrences SET status='COMPLETED',journal_id=:journal,executed_at=now(),failure_reason=NULL,updated_at=now() WHERE id=:id").param("journal",posting.journalId()).param("id",d.occurrenceId).update();
        jdbc.sql("UPDATE deposit_accounts SET total_contributed_minor=total_contributed_minor+:amount,next_due_date=(SELECT min(scheduled_date) FROM deposit_installment_occurrences WHERE deposit_account_id=:deposit AND status='SCHEDULED'),version=version+1,updated_at=now() WHERE id=:deposit").param("amount",d.amount).param("deposit",d.depositId).update();
        jdbc.sql("INSERT INTO deposit_posting_references(deposit_account_id,operation_type,operation_key,journal_id) VALUES(:deposit,'INSTALLMENT',:key,:journal)").param("deposit",d.depositId).param("key",d.key).param("journal",posting.journalId()).update(); return true;
    }
    private void fail(Due d,String reason,boolean retry){jdbc.sql("UPDATE deposit_installment_occurrences SET status=:status,failure_reason=:reason,claimed_at=CASE WHEN :retry THEN now() ELSE NULL END,updated_at=now() WHERE id=:id").param("status",retry?"SCHEDULED":"MISSED").param("reason",reason).param("retry",retry).param("id",d.occurrenceId).update();}
    private record Due(long occurrenceId,long depositId,int sequence,long amount,String currency,String key,String reference,String depositStatus,long depositLedger,int graceDays,String policy,long fundingLedger,long availableMinor,String fundingStatus,boolean debitRestricted,LocalDate scheduledDate){}
}
