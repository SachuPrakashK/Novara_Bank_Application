package com.novara.banking.transfer;

import com.novara.banking.audit.AuditService;
import com.novara.banking.ledger.JournalCommand;
import com.novara.banking.ledger.JournalLineCommand;
import com.novara.banking.ledger.LedgerPostingService;
import com.novara.banking.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class InternalTransferService {
    private static final String OPERATION = "INTERNAL_TRANSFER";
    private final JdbcClient jdbc; private final LedgerPostingService ledger; private final AuditService audit;
    public InternalTransferService(JdbcClient jdbc,LedgerPostingService ledger,AuditService audit){this.jdbc=jdbc;this.ledger=ledger;this.audit=audit;}

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public TransferView execute(String actor,String idempotencyKey,InternalTransferRequest request,String correlationId){
        validateKey(idempotencyKey); validateRequest(request); var requestHash=hash(request);
        var scopedKey=actor+"|"+OPERATION+"|"+idempotencyKey;
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:key,0))").param("key",scopedKey).query(Integer.class).single();
        var existingHash=jdbc.sql("SELECT request_hash FROM idempotency_records WHERE principal_subject=:actor AND operation_type=:operation AND idempotency_key=:key")
            .param("actor",actor).param("operation",OPERATION).param("key",idempotencyKey).query(String.class).optional();
        if(existingHash.isPresent()){
            if(!existingHash.get().equals(requestHash)) throw new DomainException(HttpStatus.CONFLICT,"IDEMPOTENCY_KEY_REUSED","The idempotency key was already used for a different instruction.");
            return requireByIdempotency(actor,idempotencyKey);
        }
        var locked=lockAccounts(request.sourceAccountId(),request.destinationAccountId());
        var source=locked.stream().filter(a->a.publicId().equals(request.sourceAccountId())).findFirst().orElseThrow(()->notFound("Source account was not found."));
        var destination=locked.stream().filter(a->a.publicId().equals(request.destinationAccountId())).findFirst().orElseThrow(()->notFound("Destination account was not found."));
        authorizeDebit(actor,source.internalId());
        ensureEligible(source,true); ensureEligible(destination,false);
        if(!source.currency().equals(request.currency())||!destination.currency().equals(request.currency())) throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"CURRENCY_MISMATCH","Transfer currency does not match both accounts.");
        if(source.availableMinor()<request.amountMinor()) throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"INSUFFICIENT_FUNDS","The account does not have sufficient available funds.");
        var transferId=UUID.randomUUID(); var reference="TRF-"+transferId.toString().substring(0,8).toUpperCase();
        jdbc.sql("INSERT INTO idempotency_records(principal_subject,operation_type,idempotency_key,request_hash,resource_type,resource_public_id,expires_at) VALUES(:actor,:operation,:key,:hash,'TRANSFER',:resource,now()+interval '24 hours')")
            .param("actor",actor).param("operation",OPERATION).param("key",idempotencyKey).param("hash",requestHash).param("resource",transferId).update();
        var posting=ledger.post(new JournalCommand("BATCH-"+reference,reference,request.currency(),"Internal transfer",List.of(
            new JournalLineCommand(source.ledgerAccountId(),JournalLineCommand.Direction.DEBIT,request.amountMinor(),request.currency()),
            new JournalLineCommand(destination.ledgerAccountId(),JournalLineCommand.Direction.CREDIT,request.amountMinor(),request.currency())),null));
        var transferInternalId=jdbc.sql("""
          INSERT INTO transfers(public_id,source_account_id,destination_account_id,amount_minor,currency,status,journal_id,initiating_subject,operation_type,idempotency_key,request_hash,completed_at)
          VALUES(:publicId,:source,:destination,:amount,:currency,'COMPLETED',:journal,:actor,:operation,:key,:hash,now()) RETURNING id
          """).param("publicId",transferId).param("source",source.internalId()).param("destination",destination.internalId()).param("amount",request.amountMinor()).param("currency",request.currency()).param("journal",posting.journalId()).param("actor",actor).param("operation",OPERATION).param("key",idempotencyKey).param("hash",requestHash).query(Long.class).single();
        jdbc.sql("UPDATE idempotency_records SET response_status=201 WHERE principal_subject=:actor AND operation_type=:operation AND idempotency_key=:key")
            .param("actor",actor).param("operation",OPERATION).param("key",idempotencyKey).update();
        jdbc.sql("INSERT INTO outbox_events(aggregate_type,aggregate_id,event_type,payload) VALUES('TRANSFER',:id,'InternalTransferCompleted',jsonb_build_object('transferId',CAST(:id AS text)))").param("id",transferId).update();
        audit.record(actor,"INTERNAL_TRANSFER","TRANSFER",transferId,"SUCCESS",correlationId,"{\"ownershipVerified\":true}");
        return requireByInternalId(transferInternalId);
    }

    public TransferView requireOwned(String actor,UUID transferId){
        return jdbc.sql(VIEW_SQL+" WHERE t.public_id=:id AND c.identity_subject=:actor").param("id",transferId).param("actor",actor).query(this::map).optional().orElseThrow(()->notFound("Transfer was not found."));
    }
    public TransferView requireByIdempotency(String actor,String key){
        return jdbc.sql(VIEW_SQL+" WHERE t.idempotency_key=:key AND t.operation_type=:operation AND t.initiating_subject=:actor AND c.identity_subject=:actor")
            .param("key",key).param("operation",OPERATION).param("actor",actor).query(this::map).optional().orElseThrow(()->notFound("Transfer was not found."));
    }
    private TransferView requireByInternalId(long id){return jdbc.sql(VIEW_SQL_NO_OWNER+" WHERE t.id=:id").param("id",id).query(this::map).single();}
    private List<LockedAccount> lockAccounts(UUID first,UUID second){
        return jdbc.sql("""
          SELECT a.id,a.public_id,a.currency,a.status::text,a.debit_restricted,a.credit_restricted,a.ledger_account_id,
            b.balance_minor-a.minimum_balance_minor+a.overdraft_limit_minor-
              COALESCE((SELECT sum(h.amount_minor) FROM account_holds h WHERE h.account_id=a.id AND h.status='ACTIVE' AND h.expires_at>now()),0)
          FROM bank_accounts a JOIN ledger_balances b ON b.ledger_account_id=a.ledger_account_id
          WHERE a.public_id IN (:first,:second) ORDER BY a.public_id FOR UPDATE OF a,b
          """).param("first",first).param("second",second).query((rs,row)->new LockedAccount(rs.getLong(1),rs.getObject(2,UUID.class),rs.getString(3),rs.getString(4),rs.getBoolean(5),rs.getBoolean(6),rs.getLong(7),rs.getLong(8))).list();
    }
    private void authorizeDebit(String actor,long accountId){
        var allowed=jdbc.sql("SELECT count(*) FROM account_owners o JOIN customers c ON c.id=o.customer_id WHERE o.account_id=:account AND c.identity_subject=:actor AND c.status='ACTIVE' AND o.can_debit=true").param("account",accountId).param("actor",actor).query(Long.class).single();
        if(allowed!=1) throw new DomainException(HttpStatus.NOT_FOUND,"ACCOUNT_NOT_FOUND","Source account was not found.");
    }
    private void ensureEligible(LockedAccount account,boolean debit){if(!account.status().equals("ACTIVE")||(debit?account.debitRestricted():account.creditRestricted()))throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"ACCOUNT_RESTRICTED","An account is not eligible for this transfer.");}
    private void validateRequest(InternalTransferRequest request){if(request.amountMinor()<=0)throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"INVALID_AMOUNT","Transfer amount must be positive.");if(request.sourceAccountId().equals(request.destinationAccountId()))throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"SAME_ACCOUNT","Source and destination accounts must differ.");if(!"INR".equals(request.currency()))throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"UNSUPPORTED_CURRENCY","Only INR is supported.");}
    private void validateKey(String key){if(key==null||!key.matches("[A-Za-z0-9_-]{16,100}"))throw new DomainException(HttpStatus.BAD_REQUEST,"INVALID_IDEMPOTENCY_KEY","A valid Idempotency-Key header is required.");}
    private String hash(InternalTransferRequest request){try{var value=request.sourceAccountId()+"|"+request.destinationAccountId()+"|"+request.amountMinor()+"|"+request.currency()+"|"+(request.note()==null?"":request.note());return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}}
    private DomainException notFound(String message){return new DomainException(HttpStatus.NOT_FOUND,"TRANSFER_NOT_FOUND",message);}
    private TransferView map(java.sql.ResultSet rs,int row)throws java.sql.SQLException{return new TransferView(rs.getObject(1,UUID.class),rs.getString(2),rs.getObject(3,UUID.class),rs.getObject(4,UUID.class),rs.getLong(5),rs.getString(6),rs.getString(7),rs.getTimestamp(8).toInstant(),rs.getTimestamp(9)==null?null:rs.getTimestamp(9).toInstant());}
    private record LockedAccount(long internalId,UUID publicId,String currency,String status,boolean debitRestricted,boolean creditRestricted,long ledgerAccountId,long availableMinor){}
    private static final String VIEW_SQL_NO_OWNER="SELECT t.public_id,t.status::text,s.public_id,d.public_id,t.amount_minor,t.currency,j.journal_reference,t.created_at,t.completed_at FROM transfers t JOIN bank_accounts s ON s.id=t.source_account_id JOIN bank_accounts d ON d.id=t.destination_account_id LEFT JOIN journals j ON j.id=t.journal_id";
    private static final String VIEW_SQL=VIEW_SQL_NO_OWNER+" JOIN account_owners o ON o.account_id=t.source_account_id JOIN customers c ON c.id=o.customer_id";
}
