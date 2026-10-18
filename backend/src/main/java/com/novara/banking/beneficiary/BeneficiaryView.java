package com.novara.banking.beneficiary;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryView(UUID id, String name, String nickname, String displayName, String bankName,
                              String maskedAccount, String routingCode, String transferMethod, String status,
                              boolean favorite, Instant availableFrom, Instant createdAt, Instant updatedAt) {}

