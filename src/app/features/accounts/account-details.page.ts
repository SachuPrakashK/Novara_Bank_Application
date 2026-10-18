import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { BANKING_API } from '../../core/api/banking-api';
import { MoneyComponent } from '../../shared/ui/money.component';
@Component({ selector:'app-account-details',imports:[RouterLink,MoneyComponent],template:`<a class="back" routerLink="/accounts">← All accounts</a>@if(account();as account){<header class="feature-heading"><div><span class="overline">{{account.type.replace('_',' ')}}</span><h1>{{account.nickname}}</h1><p>{{account.maskedNumber}} · {{account.status}}</p></div></header><section class="detail-card"><span>Available balance</span><novara-money [value]="account.balance.available"/><dl><div><dt>Ledger balance</dt><dd><novara-money [value]="account.balance.ledger"/></dd></div><div><dt>Account number</dt><dd>{{account.maskedNumber}}</dd></div><div><dt>Account detail</dt><dd>{{account.detail}}</dd></div><div><dt>Branch</dt><dd>Novara Digital Branch</dd></div></dl></section>}`,styleUrl:'../feature-page.scss',changeDetection:ChangeDetectionStrategy.OnPush})
export class AccountDetailsPage { private readonly id=inject(ActivatedRoute).snapshot.paramMap.get('accountId'); protected readonly account=toSignal(inject(BANKING_API).getAccounts().pipe(map(items=>items.find(item=>item.id===this.id)))); }
