package com.novara.banking.ledger;
public record JournalLineCommand(long ledgerAccountId, Direction direction, long amountMinor, String currency) {
    public enum Direction { DEBIT, CREDIT }
}
