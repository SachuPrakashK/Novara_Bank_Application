package com.novara.banking.beneficiary;

import jakarta.validation.constraints.NotNull;

public record FavoriteBeneficiaryRequest(@NotNull Boolean favorite) {}

