import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { LOANS_API } from '../../core/loans/loans-api';
import type { LoanApplication, LoanProduct, LoanQuote } from '../../core/loans/loan.models';
import { IconComponent } from '../../shared/ui/icon.component';
import { MoneyComponent } from '../../shared/ui/money.component';

@Component({
  selector: 'app-loans',
  imports: [DecimalPipe, IconComponent, MoneyComponent, ReactiveFormsModule],
  templateUrl: './loans.page.html',
  styleUrl: './loans.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoansPage {
  private readonly api = inject(LOANS_API);
  protected readonly products = signal<readonly LoanProduct[]>([]);
  protected readonly applications = signal<readonly LoanApplication[]>([]);
  protected readonly quote = signal<LoanQuote | null>(null);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly error = signal('');
  protected readonly submitted = computed(
    () => this.applications().filter((application) => application.status === 'SUBMITTED').length,
  );
  protected readonly form = new FormGroup({
    productId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    amount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)],
    }),
    tenureMonths: new FormControl(12, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
    purposeCategory: new FormControl('PERSONAL', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    declaration: new FormControl(false, {
      nonNullable: true,
      validators: [Validators.requiredTrue],
    }),
  });

  constructor() {
    void this.load();
  }

  protected selectProduct(product: LoanProduct): void {
    this.form.patchValue({
      productId: product.id,
      tenureMonths: product.minimumTenureMonths,
      purposeCategory: product.category,
    });
    this.quote.set(null);
  }

  protected async calculate(): Promise<void> {
    const { productId, amount, tenureMonths } = this.form.getRawValue();
    if (!productId || !amount || this.form.controls.amount.invalid || this.form.controls.tenureMonths.invalid) {
      this.form.controls.productId.markAsTouched();
      this.form.controls.amount.markAsTouched();
      this.form.controls.tenureMonths.markAsTouched();
      this.error.set('Enter an amount and tenure within the selected product limits.');
      return;
    }
    this.submitting.set(true);
    this.error.set('');
    try {
      this.quote.set(
        await firstValueFrom(
          this.api.quote(productId, Math.round(Number(amount) * 100), tenureMonths),
        ),
      );
    } catch {
      this.error.set('The authoritative quote could not be prepared. Check the product limits.');
    } finally {
      this.submitting.set(false);
    }
  }

  protected async submitApplication(): Promise<void> {
    const quote = this.quote();
    if (!quote || this.form.controls.declaration.invalid || this.submitting()) {
      this.form.controls.declaration.markAsTouched();
      this.error.set('Accept the sandbox declaration before submitting.');
      return;
    }
    this.submitting.set(true);
    this.error.set('');
    try {
      const application = await firstValueFrom(
        this.api.apply(quote.id, this.form.controls.purposeCategory.value),
      );
      this.applications.update((items) => [application, ...items]);
      this.quote.set(null);
      this.form.controls.declaration.setValue(false);
    } catch {
      this.error.set('The application was not submitted. Your quote may have expired or recent MFA may be required.');
    } finally {
      this.submitting.set(false);
    }
  }

  protected productFor(id: string): LoanProduct | undefined {
    return this.products().find((product) => product.id === id);
  }

  private async load(): Promise<void> {
    try {
      const [products, applications] = await Promise.all([
        firstValueFrom(this.api.products()),
        firstValueFrom(this.api.applications()),
      ]);
      this.products.set(products);
      this.applications.set(applications);
      if (products[0]) this.selectProduct(products[0]);
    } catch {
      this.error.set('Loan products are temporarily unavailable.');
    } finally {
      this.loading.set(false);
    }
  }
}
