import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { BANKING_API } from '../../core/api/banking-api';
import { NOVARA_CONFIG } from '../../core/config/app-config';
import { PAYMENTS_API } from '../../core/payments/payments-api';
import { PaymentStateService } from '../../core/payments/payment-state.service';
import { parseInrInput } from '../../core/payments/money-input';

@Component({ selector: 'app-transfer', imports: [ReactiveFormsModule, RouterLink], templateUrl: './transfer.page.html', styleUrl: './payments.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class TransferPage {
  private readonly config = inject(NOVARA_CONFIG);
  private readonly state = inject(PaymentStateService);
  private readonly router = inject(Router);
  private readonly bankingApi = inject(BANKING_API);
  private readonly paymentsApi = inject(PAYMENTS_API);
  private readonly accounts$ = this.bankingApi.getAccounts();
  protected readonly accounts = toSignal(this.accounts$, { initialValue: [] });
  protected readonly internalOnly = !this.config.useMockApi;
  protected readonly beneficiaries = toSignal(this.paymentsApi.getBeneficiaries(), { initialValue: [] });
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly form = new FormGroup({
    sourceAccountId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    beneficiaryId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    destinationType: new FormControl<'OWN_ACCOUNT'|'BENEFICIARY'>('BENEFICIARY', { nonNullable: true }),
    amount: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    method: new FormControl<'INTERNAL' | 'UPI' | 'IMPS' | 'NEFT' | 'RTGS'>(this.config.useMockApi ? 'IMPS' : 'INTERNAL', { nonNullable: true, validators: [Validators.required] }),
    note: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(80)] }),
  });

  protected async review(): Promise<void> {
    const amount = parseInrInput(this.form.controls.amount.value);
    if (this.form.invalid || !amount || this.loading()) { this.form.markAllAsTouched(); this.error.set(!amount ? 'Enter a valid amount with no more than two decimal places.' : 'Complete the required payment details.'); return; }
    if (this.form.controls.destinationType.value==='OWN_ACCOUNT'&&this.form.controls.sourceAccountId.value === this.form.controls.beneficiaryId.value) { this.error.set('Choose a different destination account.'); return; }
    this.loading.set(true); this.error.set('');
    try { await this.state.prepare({ sourceAccountId: this.form.controls.sourceAccountId.value, beneficiaryId: this.form.controls.beneficiaryId.value, destinationType:this.form.controls.destinationType.value, amount, method: this.form.controls.method.value, note: this.form.controls.note.value.trim() || undefined }); await this.router.navigate(['/payments/review']); }
    catch { this.error.set('We could not prepare this transfer. Check beneficiary availability and try again.'); }
    finally { this.loading.set(false); }
  }
}
