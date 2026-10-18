package com.novara.banking.reconciliation;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ReconciliationService {
    private final JdbcClient jdbc;
    public ReconciliationService(JdbcClient jdbc){this.jdbc=jdbc;}

    @Transactional
    public ReconciliationResult run(){
        var run=jdbc.sql("INSERT INTO reconciliation_runs(status) VALUES('RUNNING') RETURNING id,public_id").query((rs,row)->new Run(rs.getLong(1),rs.getObject(2,UUID.class))).single();
        int count=0;
        count+=record(run.id(),"UNBALANCED_JOURNAL","JOURNAL","""
          SELECT j.public_id::text,jsonb_build_object('debits',COALESCE(sum(l.amount_minor) FILTER(WHERE l.direction='DEBIT'),0),'credits',COALESCE(sum(l.amount_minor) FILTER(WHERE l.direction='CREDIT'),0))
          FROM journals j LEFT JOIN journal_lines l ON l.journal_id=j.id WHERE j.status IN('POSTED','REVERSED') GROUP BY j.id HAVING COALESCE(sum(l.amount_minor) FILTER(WHERE l.direction='DEBIT'),0)<>COALESCE(sum(l.amount_minor) FILTER(WHERE l.direction='CREDIT'),0)
          """);
        count+=record(run.id(),"BALANCE_PROJECTION_MISMATCH","LEDGER_ACCOUNT","""
          SELECT la.public_id::text,jsonb_build_object('projected',b.balance_minor,'calculated',COALESCE(sum(CASE WHEN la.classification IN('ASSET','EXPENSE') THEN CASE WHEN l.direction='DEBIT' THEN l.amount_minor ELSE -l.amount_minor END ELSE CASE WHEN l.direction='CREDIT' THEN l.amount_minor ELSE -l.amount_minor END END),0))
          FROM ledger_accounts la JOIN ledger_balances b ON b.ledger_account_id=la.id LEFT JOIN journal_lines l ON l.ledger_account_id=la.id LEFT JOIN journals j ON j.id=l.journal_id AND j.status IN('POSTED','REVERSED') GROUP BY la.id,b.balance_minor HAVING b.balance_minor<>COALESCE(sum(CASE WHEN j.id IS NULL THEN 0 WHEN la.classification IN('ASSET','EXPENSE') THEN CASE WHEN l.direction='DEBIT' THEN l.amount_minor ELSE -l.amount_minor END ELSE CASE WHEN l.direction='CREDIT' THEN l.amount_minor ELSE -l.amount_minor END END),0)
          """);
        count+=record(run.id(),"STALE_TRANSFER_STATE","TRANSFER","SELECT public_id::text,jsonb_build_object('status',status,'createdAt',created_at) FROM transfers WHERE status='PROCESSING' AND created_at<now()-interval '5 minutes'");
        count+=record(run.id(),"ORPHANED_COMPLETED_TRANSFER","TRANSFER","SELECT t.public_id::text,jsonb_build_object('journalId',t.journal_id) FROM transfers t LEFT JOIN journals j ON j.id=t.journal_id WHERE t.status='COMPLETED' AND (j.id IS NULL OR j.status NOT IN('POSTED','REVERSED'))");
        jdbc.sql("UPDATE reconciliation_runs SET status='COMPLETED',completed_at=now(),exception_count=:count WHERE id=:id").param("count",count).param("id",run.id()).update();
        return new ReconciliationResult(run.publicId(),"COMPLETED",count);
    }
    private int record(long runId,String type,String entityType,String query){
        var rows=jdbc.sql(query).query((rs,row)->new ExceptionRow(rs.getString(1),rs.getString(2))).list();
        rows.forEach(item->jdbc.sql("INSERT INTO reconciliation_exceptions(run_id,exception_type,entity_type,entity_reference,details) VALUES(:run,:type,:entityType,:reference,CAST(:details AS jsonb))").param("run",runId).param("type",type).param("entityType",entityType).param("reference",item.reference()).param("details",item.details()).update());
        return rows.size();
    }
    private record Run(long id,UUID publicId){} private record ExceptionRow(String reference,String details){}
}
