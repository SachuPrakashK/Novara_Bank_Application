package com.novara.banking.ledger;
import java.util.UUID;
public record PostingResult(long journalId, UUID journalPublicId, String journalReference) {}
