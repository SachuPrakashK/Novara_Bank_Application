import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { BANKING_API } from '../../core/api/banking-api';
import { AccountCardComponent } from '../../shared/ui/account-card.component';
import { MoneyComponent } from '../../shared/ui/money.component';
import { TransactionItemComponent } from '../../shared/ui/transaction-item.component';
import { IconComponent, type NovaraIcon } from '../../shared/ui/icon.component';

@Component({ selector: 'app-dashboard', imports: [RouterLink, AccountCardComponent, MoneyComponent, TransactionItemComponent,IconComponent], templateUrl: './dashboard.page.html', styleUrl: './dashboard.page.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class DashboardPage {
  protected readonly dashboard = toSignal(inject(BANKING_API).getDashboard());
  protected readonly balanceHidden = signal(false);
  protected readonly greeting = new Intl.DateTimeFormat('en-IN', { hour: 'numeric', hour12: false }).format(new Date()).startsWith('0') ? 'Good morning' : new Date().getHours() < 12 ? 'Good morning' : new Date().getHours() < 17 ? 'Good afternoon' : 'Good evening';
  protected readonly quickActions: readonly {icon:NovaraIcon;label:string;path:string}[] = [{ icon: 'arrow-up-right', label: 'Send money',path:'/payments/transfer' }, { icon: 'plus', label: 'Add money',path:'/accounts' }, { icon: 'receipt', label: 'Statements',path:'/statements' }, { icon: 'qr', label: 'Scan & pay',path:'/payments' }, { icon: 'menu', label: 'More',path:'/support' }];
}
