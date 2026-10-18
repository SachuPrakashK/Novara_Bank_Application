package com.novara.banking.account;
import java.util.UUID;
public record AccountView(UUID id, String nickname, String type, String maskedNumber, String currency, String status, long availableBalanceMinor, long ledgerBalanceMinor) {}
