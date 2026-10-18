package com.novara.banking.reconciliation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController @RequestMapping("/api/v1/operations/reconciliation")
public class ReconciliationController {
    private final ReconciliationService service;
    public ReconciliationController(ReconciliationService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAuthority('SCOPE_operations.reconcile') or hasAnyRole('OPERATIONS','ADMINISTRATOR')") ReconciliationResult run(){return service.run();}
}
