package com.novara.banking.customer;
import java.util.UUID;
public record CustomerProfile(UUID id, String displayName, String status) {}
