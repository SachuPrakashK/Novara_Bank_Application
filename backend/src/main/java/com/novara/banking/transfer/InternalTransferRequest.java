package com.novara.banking.transfer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record InternalTransferRequest(@NotNull UUID sourceAccountId,@NotNull UUID destinationAccountId,@Positive long amountMinor,@NotNull String currency,@Size(max=140) String note) {}
