import type { Money, PaymentMethod } from '../models/banking.models';

export type BeneficiaryStatus = 'ACTIVE' | 'COOLING_PERIOD' | 'INACTIVE';

export interface PaymentBeneficiary {
  readonly id: string;
  readonly name: string;
  readonly bankName: string;
  readonly maskedAccount: string;
  readonly favorite: boolean;
  readonly status: BeneficiaryStatus;
  readonly availableFrom?: string;
}

export interface CreateBeneficiaryCommand {
  readonly name: string;
  readonly accountNumber: string;
  readonly confirmAccountNumber: string;
  readonly routingCode: string;
  readonly nickname?: string;
}

export interface PaymentDraft {
  readonly sourceAccountId: string;
  readonly beneficiaryId: string;
  readonly amount: Money;
  readonly method: PaymentMethod;
  readonly note?: string;
}

export interface PaymentQuote {
  readonly quoteId: string;
  readonly sourceAccountId: string;
  readonly sourceLabel: string;
  readonly sourceMaskedNumber: string;
  readonly beneficiaryId: string;
  readonly beneficiaryName: string;
  readonly beneficiaryMaskedNumber: string;
  readonly amount: Money;
  readonly fee: Money;
  readonly totalDebit: Money;
  readonly method: PaymentMethod;
  readonly processingMessage: string;
  readonly expiresAt: string;
  readonly note?: string;
}

export interface SubmitPaymentCommand {
  readonly quoteId: string;
  readonly idempotencyKey: string;
  readonly quote?: PaymentQuote;
}

export type PaymentOutcome = 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'UNKNOWN';

export interface PaymentResult {
  readonly outcome: PaymentOutcome;
  readonly clientRequestId: string;
  readonly paymentId?: string;
  readonly reference?: string;
  readonly submittedAt: string;
  readonly message: string;
}
