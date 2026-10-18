package com.novara.banking.beneficiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBeneficiaryRequest(
    @NotBlank @Size(max=80) String name,
    @Size(max=40) String nickname,
    @NotBlank @Pattern(regexp="[0-9]{8,18}") String accountNumber,
    @NotBlank @Pattern(regexp="[A-Za-z0-9-]{5,20}") String routingCode,
    @NotBlank @Size(max=100) String bankName,
    @NotBlank @Pattern(regexp="INTERNAL|UPI|IMPS|NEFT|RTGS") String transferMethod) {}

