import { ChangeDetectionStrategy, Component, HostListener, effect, inject, signal, viewChild } from '@angular/core';
import type { ElementRef } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { ErrorNotificationService } from '../core/http/error-notification.service';
import { SessionService } from '../core/session/session.service';
import { LogoComponent } from '../shared/ui/logo.component';
import { IconComponent, type NovaraIcon } from '../shared/ui/icon.component';

@Component({
  selector: 'app-shell', imports: [RouterOutlet, RouterLink, RouterLinkActive, LogoComponent, IconComponent], templateUrl: './app-shell.component.html', styleUrl: './app-shell.component.scss', changeDetection: ChangeDetectionStrategy.OnPush,
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
  protected readonly sidebarCollapsed = signal(false);
  protected readonly theme = signal<'light' | 'dark'>('light');
  protected readonly primaryNav: readonly {label:string;icon:NovaraIcon;path:string}[] = [{ label: 'Dashboard', icon: 'home', path: '/dashboard' },{ label: 'Accounts', icon: 'wallet', path: '/accounts' },{ label: 'Payments', icon: 'arrow-up-right', path: '/payments' },{ label: 'Cards', icon: 'card', path: '/cards' },{ label: 'Deposits', icon: 'deposit', path: '/deposits' },{ label: 'Loans', icon: 'loan', path: '/loans' },{ label: 'Investments', icon: 'chart', path: '/investments' },{ label: 'Statements', icon: 'receipt', path: '/statements' },{ label: 'Account services', icon: 'help', path: '/account-services' },{ label: 'Notifications', icon: 'bell', path: '/notifications' },{ label: 'Preferences', icon: 'limits', path: '/preferences' },{ label: 'Support', icon: 'help', path: '/support' }];
  constructor() { effect(() => { if (this.session.sessionState() === 'warning' || this.session.sessionState() === 'expired') this.sessionAction()?.nativeElement.focus(); }); }
  @HostListener('document:keydown.escape') protected closeMobileNavigation(): void { this.navOpen.set(false); }
  protected toggleSidebar(): void { this.sidebarCollapsed.update((collapsed) => !collapsed); }
  protected toggleTheme(): void { const next = this.theme() === 'light' ? 'dark' : 'light'; this.theme.set(next); document.documentElement.dataset['theme'] = next; }
  protected async logout(): Promise<void> { await this.auth.signOut(); await this.router.navigate(['/login']); }
  protected async continueSession(): Promise<void> { if (this.continuing()) return; this.continuing.set(true); await this.auth.continueSession(); this.continuing.set(false); }
  protected async returnToSignIn(): Promise<void> { this.auth.acknowledgeExpiry(); await this.router.navigate(['/login']); }
}
