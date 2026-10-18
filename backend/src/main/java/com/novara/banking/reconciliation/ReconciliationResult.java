package com.novara.banking.reconciliation;
import java.util.UUID;
public record ReconciliationResult(UUID runId,String status,int exceptionCount) {}
