import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { BANKING_API } from '../../core/api/banking-api';
import { AccountCardComponent } from '../../shared/ui/account-card.component';
import { MoneyComponent } from '../../shared/ui/money.component';
import { TransactionItemComponent } from '../../shared/ui/transaction-item.component';

@Component({ selector: 'app-dashboard', imports: [RouterLink, AccountCardComponent, MoneyComponent, TransactionItemComponent], templateUrl: './dashboard.page.html', styleUrl: './dashboard.page.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class DashboardPage {
  protected readonly dashboard = toSignal(inject(BANKING_API).getDashboard());
  protected readonly balanceHidden = signal(false);
  protected readonly greeting = new Intl.DateTimeFormat('en-IN', { hour: 'numeric', hour12: false }).format(new Date()).startsWith('0') ? 'Good morning' : new Date().getHours() < 12 ? 'Good morning' : new Date().getHours() < 17 ? 'Good afternoon' : 'Good evening';
  protected readonly quickActions = [{ icon: '↗', label: 'Send money' }, { icon: '+', label: 'Add money' }, { icon: '▤', label: 'Pay bills' }, { icon: '⌗', label: 'Scan & pay' }, { icon: '•••', label: 'More' }];
}
