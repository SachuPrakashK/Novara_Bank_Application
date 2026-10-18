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
  readonly destinationType?: 'OWN_ACCOUNT' | 'BENEFICIARY';
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
  readonly status?: PaymentStatus;
  readonly destinationType?: 'OWN_ACCOUNT' | 'BENEFICIARY';
  readonly requiresStepUp?: boolean;
  readonly createdAt?: string;
  readonly completedAt?: string;
  readonly reference?: string;
  readonly timeline?: readonly PaymentStatusEvent[];
}

export type PaymentStatus = 'PENDING_AUTHORIZATION' | 'SCHEDULED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REQUIRES_REVIEW';
export interface PaymentStatusEvent { readonly status: PaymentStatus; readonly reasonCode?: string; readonly occurredAt: string; }
export interface PaymentPage { readonly items: readonly PaymentQuote[]; readonly page: number; readonly size: number; readonly total: number; }
export interface PaymentLimits { readonly perPaymentMinor: number; readonly dailyLimitMinor: number; readonly usedTodayMinor: number; readonly availableTodayMinor: number; readonly customerSelectedDailyMinor?: number; readonly currency: 'INR'; readonly bankingTimezone: string; }
export interface PaymentSchedule { readonly id: string; readonly status: 'ACTIVE'|'PAUSED'|'COMPLETED'|'CANCELLED'|'REQUIRES_REVIEW'; readonly recurrence: 'ONCE'|'WEEKLY'|'MONTHLY'; readonly timezone: string; readonly sourceAccountId: string; readonly sourceMaskedNumber: string; readonly destinationName: string; readonly destinationMaskedNumber: string; readonly amountMinor: number; readonly currency: 'INR'; readonly nextExecutionAt: string; readonly endAt?: string; readonly maximumOccurrences?: number; readonly executedOccurrences: number; readonly description?: string; }
export interface CreateScheduleCommand { readonly sourceAccountId: string; readonly destinationType: 'OWN_ACCOUNT'|'BENEFICIARY'; readonly destinationId: string; readonly amountMinor: number; readonly currency: 'INR'; readonly description?: string; readonly firstExecutionAt: string; readonly timezone: string; readonly recurrence: 'ONCE'|'WEEKLY'|'MONTHLY'; readonly endAt?: string; readonly maximumOccurrences?: number; }

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
