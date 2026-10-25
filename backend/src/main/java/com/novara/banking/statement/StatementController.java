package com.novara.banking.statement;

import com.novara.banking.audit.AuditService;
import com.novara.banking.identity.CurrentIdentity;
import com.novara.banking.statement.StatementModels.*;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/v1") public class StatementController {
 private final StatementService service;private final StatementExportService exports;private final CurrentIdentity identities;private final AuditService audit;private final DocumentRateLimitService limits;
 public StatementController(StatementService service,StatementExportService exports,CurrentIdentity identities,AuditService audit,DocumentRateLimitService limits){this.service=service;this.exports=exports;this.identities=identities;this.audit=audit;this.limits=limits;}
 @GetMapping("/statements/accounts") List<Account> accounts(Authentication a){return service.accounts(identities.from(a));}
 @GetMapping("/statements/transactions") Page transactions(Authentication a,@RequestParam UUID accountId,@RequestParam LocalDate from,@RequestParam LocalDate to,@RequestParam(required=false) String direction,@RequestParam(required=false) String type,@RequestParam(required=false) String search,@RequestParam(defaultValue="newest") String sort,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){return service.transactions(identities.from(a),accountId,from,to,direction,type,search,sort,page,size);}
 @GetMapping("/statements/summary") Summary summary(Authentication a,@RequestParam UUID accountId,@RequestParam LocalDate from,@RequestParam LocalDate to){return service.summary(identities.from(a),accountId,from,to);}
 public record GenerateRequest(UUID accountId,LocalDate from,LocalDate to){}
 @PostMapping("/statements/generations") ResponseEntity<Generation> generate(Authentication a,@RequestHeader("Idempotency-Key") String key,@RequestBody GenerateRequest r){var i=identities.from(a);limits.generation(i);return ResponseEntity.status(HttpStatus.CREATED).body(service.generate(i,r.accountId(),r.from(),r.to(),key,MDC.get("correlationId")));}
 @GetMapping("/statements/{id}") StatementService.Snapshot statement(Authentication a,@PathVariable UUID id){return service.snapshot(identities.from(a),id);}
 @GetMapping("/statements/{id}/pdf") ResponseEntity<byte[]> pdf(Authentication a,@PathVariable UUID id){var i=identities.from(a);limits.download(i);var s=service.snapshot(i,id);audit.record(i.subject(),"STATEMENT_PDF_DOWNLOADED","STATEMENT",id,"SUCCESS",MDC.get("correlationId"),"{}");return file(exports.statementPdf(s),MediaType.APPLICATION_PDF,"Novara-Statement-"+id.toString().substring(0,8)+".pdf");}
 @GetMapping("/statements/{id}/csv") ResponseEntity<byte[]> csv(Authentication a,@PathVariable UUID id){var i=identities.from(a);limits.download(i);var s=service.snapshot(i,id);audit.record(i.subject(),"STATEMENT_CSV_DOWNLOADED","STATEMENT",id,"SUCCESS",MDC.get("correlationId"),"{}");return file(exports.csv(s),MediaType.parseMediaType("text/csv;charset=UTF-8"),"Novara-Statement-"+id.toString().substring(0,8)+".csv");}
 @GetMapping("/receipts/{id}") ResponseEntity<Receipt> receipt(Authentication a,@PathVariable UUID id){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.receipt(identities.from(a),id));}
 @GetMapping("/receipts/{id}/pdf") ResponseEntity<byte[]> receiptPdf(Authentication a,@PathVariable UUID id){var i=identities.from(a);limits.download(i);var r=service.receipt(i,id);audit.record(i.subject(),"RECEIPT_PDF_DOWNLOADED","JOURNAL",id,"SUCCESS",MDC.get("correlationId"),"{}");return file(exports.receiptPdf(r),MediaType.APPLICATION_PDF,"Novara-Receipt-"+id.toString().substring(0,8)+".pdf");}
 private ResponseEntity<byte[]> file(byte[] body,MediaType type,String name){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA,"no-cache").header("X-Content-Type-Options","nosniff").header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+name+"\"").contentType(type).contentLength(body.length).body(body);}
}
