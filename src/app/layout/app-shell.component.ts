import { ChangeDetectionStrategy, Component, effect, inject, signal, viewChild } from '@angular/core';
import type { ElementRef } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { ErrorNotificationService } from '../core/http/error-notification.service';
import { SessionService } from '../core/session/session.service';
import { LogoComponent } from '../shared/ui/logo.component';

@Component({
  selector: 'app-shell', imports: [RouterOutlet, RouterLink, RouterLinkActive, LogoComponent], templateUrl: './app-shell.component.html', styleUrl: './app-shell.component.scss', changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly session = inject(SessionService);
  protected readonly notifications = inject(ErrorNotificationService);
  protected readonly customer = this.auth.customer;
  protected readonly continuing = signal(false);
  private readonly sessionAction = viewChild<ElementRef<HTMLButtonElement>>('sessionAction');
  protected readonly navOpen = signal(false);
  protected readonly theme = signal<'light' | 'dark'>('light');
  protected readonly primaryNav = [{ label: 'Dashboard', icon: '⌂', path: '/dashboard' },{ label: 'Accounts', icon: '◫', path: '/accounts' },{ label: 'Payments', icon: '↗', path: '/payments' },{ label: 'Cards', icon: '▣', path: '/cards' },{ label: 'Deposits', icon: '◇', path: '/deposits' },{ label: 'Loans', icon: '◎', path: '/loans' },{ label: 'Investments', icon: '⌁', path: '/investments' },{ label: 'Statements', icon: '▤', path: '/statements' },{ label: 'Support', icon: '?', path: '/support' }];
  constructor() { effect(() => { if (this.session.sessionState() === 'warning' || this.session.sessionState() === 'expired') this.sessionAction()?.nativeElement.focus(); }); }
  protected toggleTheme(): void { const next = this.theme() === 'light' ? 'dark' : 'light'; this.theme.set(next); document.documentElement.dataset['theme'] = next; }
  protected async logout(): Promise<void> { await this.auth.signOut(); await this.router.navigate(['/login']); }
  protected async continueSession(): Promise<void> { if (this.continuing()) return; this.continuing.set(true); await this.auth.continueSession(); this.continuing.set(false); }
  protected async returnToSignIn(): Promise<void> { this.auth.acknowledgeExpiry(); await this.router.navigate(['/login']); }
}
