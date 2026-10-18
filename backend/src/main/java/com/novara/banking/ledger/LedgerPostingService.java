package com.novara.banking.ledger;

import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.sql.Types;

@Service
public class LedgerPostingService {
    private final JdbcClient jdbc;
    public LedgerPostingService(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional
    public PostingResult post(JournalCommand command) {
        validate(command);
        var batchId = jdbc.sql("INSERT INTO posting_batches(posting_reference) VALUES(:reference) RETURNING id").param("reference",command.postingReference()).query(Long.class).single();
        var journal = jdbc.sql("""
            INSERT INTO journals(posting_batch_id,journal_reference,currency,status,description,reversal_of_journal_id)
            VALUES(:batch,:reference,:currency,'PENDING',:description,:reversal) RETURNING id,public_id
            """).param("batch",batchId).param("reference",command.journalReference()).param("currency",command.currency()).param("description",command.description()).param("reversal",command.reversalOfJournalId(),Types.BIGINT)
            .query((rs,row)->new PostingResult(rs.getLong(1),rs.getObject(2,java.util.UUID.class),command.journalReference())).single();
        short lineNumber=1;
        for (var line:command.lines()) {
            jdbc.sql("INSERT INTO journal_lines(journal_id,line_number,ledger_account_id,direction,amount_minor,currency) VALUES(:journal,:line,:ledger,CAST(:direction AS entry_direction),:amount,:currency)")
                .param("journal",journal.journalId()).param("line",lineNumber++).param("ledger",line.ledgerAccountId()).param("direction",line.direction().name()).param("amount",line.amountMinor()).param("currency",line.currency()).update();
            var changed=jdbc.sql("""
                UPDATE ledger_balances b SET balance_minor=balance_minor + CASE
                  WHEN la.classification IN ('ASSET','EXPENSE') THEN CASE WHEN CAST(:direction AS entry_direction)='DEBIT' THEN :amount ELSE -:amount END
                  ELSE CASE WHEN CAST(:direction AS entry_direction)='CREDIT' THEN :amount ELSE -:amount END END,
                  version=version+1,updated_at=now()
                FROM ledger_accounts la WHERE b.ledger_account_id=:ledger AND la.id=b.ledger_account_id
                """).param("direction",line.direction().name()).param("amount",line.amountMinor()).param("ledger",line.ledgerAccountId()).update();
            if(changed!=1) throw new DomainException(HttpStatus.CONFLICT,"LEDGER_ACCOUNT_MISSING","A ledger account is unavailable for posting.");
        }
        jdbc.sql("UPDATE journals SET status='POSTED',posted_at=now() WHERE id=:id").param("id",journal.journalId()).update();
        return journal;
    }

    @Transactional
    public PostingResult reverse(long journalId, String postingReference, String journalReference) {
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(:journal)").param("journal",journalId).query(Integer.class).single();
        var original=jdbc.sql("""
            SELECT j.currency,j.description FROM journals j WHERE j.id=:id AND j.status='POSTED'
            AND NOT EXISTS (SELECT 1 FROM journals r WHERE r.reversal_of_journal_id=j.id) FOR UPDATE
            """).param("id",journalId).query((rs,row)->new String[]{rs.getString(1),rs.getString(2)}).optional()
            .orElseThrow(()->new DomainException(HttpStatus.CONFLICT,"JOURNAL_NOT_REVERSIBLE","The journal cannot be reversed."));
        var lines=jdbc.sql("SELECT ledger_account_id,direction::text,amount_minor,currency FROM journal_lines WHERE journal_id=:id ORDER BY line_number").param("id",journalId)
            .query((rs,row)->new JournalLineCommand(rs.getLong(1),JournalLineCommand.Direction.valueOf(rs.getString(2).equals("DEBIT")?"CREDIT":"DEBIT"),rs.getLong(3),rs.getString(4))).list();
        var result=post(new JournalCommand(postingReference,journalReference,original[0],"Reversal: "+original[1],lines,journalId));
        return result;
    }

    private void validate(JournalCommand command) {
        if(command.lines()==null||command.lines().size()<2) throw invalid("A journal requires at least two lines.");
        long debits=0,credits=0; var accounts=new HashSet<Long>();
        try {
            for(var line:command.lines()) {
                if(line.amountMinor()<=0||!command.currency().equals(line.currency())) throw invalid("Journal lines must be positive and use the journal currency.");
                accounts.add(line.ledgerAccountId());
                if(line.direction()==JournalLineCommand.Direction.DEBIT) debits=Math.addExact(debits,line.amountMinor()); else credits=Math.addExact(credits,line.amountMinor());
            }
        } catch(ArithmeticException exception) { throw invalid("Journal totals exceed the supported monetary range."); }
        if(accounts.size()<2||debits==0||debits!=credits) throw invalid("Journal debits and credits must balance across distinct accounts.");
    }
    private DomainException invalid(String message){return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"UNBALANCED_JOURNAL",message);}
}
