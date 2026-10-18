package com.novara.banking.beneficiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBeneficiaryRequest(@NotBlank @Size(max=80) String name, @Size(max=40) String nickname) {}

