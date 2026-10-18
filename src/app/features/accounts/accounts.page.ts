import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { BANKING_API } from '../../core/api/banking-api';
import { AccountCardComponent } from '../../shared/ui/account-card.component';
@Component({ selector: 'app-accounts', imports: [AccountCardComponent], template: `<header class="feature-heading"><div><span class="overline">ACCOUNTS</span><h1>Your accounts</h1><p>Balances and details across your Novara relationship.</p></div><button>Download summary</button></header>@if(accounts();as accounts){<section class="cards">@for(account of accounts;track account.id){<novara-account-card [account]="account"/>}</section><section class="info"><h2>Account services</h2><p>Manage statements, account details, nominations and service requests from one place.</p><div><button>View statements</button><button>Account services</button></div></section>}`, styleUrl: '../feature-page.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class AccountsPage { protected readonly accounts = toSignal(inject(BANKING_API).getAccounts()); }
