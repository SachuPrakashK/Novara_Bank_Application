import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { NOVARA_CONFIG } from '../../core/config/app-config';
import { LogoComponent } from '../../shared/ui/logo.component';
import { IconComponent } from '../../shared/ui/icon.component';

@Component({ selector: 'app-login', imports: [ReactiveFormsModule, RouterLink, LogoComponent, IconComponent], templateUrl: './login.page.html', styleUrl: './auth.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class LoginPage {
  protected readonly showPassword = signal(false); protected readonly loading = signal(false); protected readonly error = signal('');
  protected readonly form = new FormGroup({ username: new FormControl('', { nonNullable: true, validators: [Validators.required] }), password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }), remember: new FormControl(false, { nonNullable: true }) });
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly demoEnabled = inject(NOVARA_CONFIG).useMockApi;
  protected continueSecurely(): void { this.auth.redirectToIdentityProvider('/dashboard'); }
  protected async submit(): Promise<void> { if (this.form.invalid || this.loading()) { this.form.markAllAsTouched(); return; } this.loading.set(true); this.error.set(''); try { await this.auth.beginSignIn(this.form.controls.username.value, this.form.controls.password.value, this.form.controls.remember.value); await this.router.navigate(['/auth/mfa']); } catch { this.error.set('We could not sign you in. Check your details or try again shortly.'); } finally { this.loading.set(false); } }
  protected enterDemo(): void { if (!this.demoEnabled) return; this.auth.useDemoSession(); void this.router.navigate(['/dashboard']); }
}
