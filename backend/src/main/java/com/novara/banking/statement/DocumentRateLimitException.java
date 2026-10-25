package com.novara.banking.statement;
public final class DocumentRateLimitException extends RuntimeException { private final long retryAfter; public DocumentRateLimitException(long retryAfter){super("The sandbox document request limit was reached.");this.retryAfter=retryAfter;}public long retryAfter(){return retryAfter;} }
