package com.novara.banking.ledger;
import java.util.List;
public record JournalCommand(String postingReference, String journalReference, String currency, String description, List<JournalLineCommand> lines, Long reversalOfJournalId) {}
