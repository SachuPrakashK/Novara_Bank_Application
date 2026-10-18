import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { IconComponent } from '../../shared/ui/icon.component';
import { LogoComponent } from '../../shared/ui/logo.component';

@Component({ selector: 'app-mfa', imports: [ReactiveFormsModule, RouterLink, LogoComponent, IconComponent], templateUrl: './mfa.page.html', styleUrl: './auth.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class MfaPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly code = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/^\d{6}$/)] });
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly destinationHint = computed(() => this.auth.pendingChallenge()?.destinationHint?.replaceAll('â€¢', '•') || '•• 42');

  protected async submit(): Promise<void> {
    if (this.code.invalid || this.loading()) { this.code.markAsTouched(); this.error.set('Enter the complete 6-digit verification code.'); return; }
    this.loading.set(true);
    this.error.set('');
    try {
      if (await this.auth.verifyMfa(this.code.value)) await this.router.navigate(['/dashboard']);
      else this.error.set('That code was not accepted. Check the six digits and try again.');
    } catch {
      this.error.set('The verification code may have expired or the service is temporarily unavailable. Return to sign in and start again.');
    } finally {
      this.loading.set(false);
    }
  }
}
