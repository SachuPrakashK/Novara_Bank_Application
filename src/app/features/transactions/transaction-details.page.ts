import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { BANKING_API } from '../../core/api/banking-api';
import { MoneyComponent } from '../../shared/ui/money.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';
@Component({selector:'app-transaction-details',imports:[DatePipe,RouterLink,MoneyComponent,StatusBadgeComponent],template:`<a class="back" routerLink="/transactions">← All transactions</a>@if(transaction();as item){<header class="feature-heading"><div><span class="overline">TRANSACTION DETAIL</span><h1>{{item.counterparty}}</h1><p>{{item.occurredAt|date:'medium'}}</p></div><novara-status-badge [tone]="item.status==='PENDING'?'pending':'success'">{{item.status}}</novara-status-badge></header><section class="detail-card"><span>Amount</span><novara-money [value]="item.amount"/><dl><div><dt>Reference</dt><dd>{{item.reference}}</dd></div><div><dt>From account</dt><dd>{{item.maskedAccount}}</dd></div><div><dt>Category</dt><dd>{{item.category}}</dd></div><div><dt>Fees</dt><dd>₹0.00</dd></div></dl><div class="detail-actions"><button>Request receipt</button><button>Get support</button></div></section>}`,styleUrl:'../feature-page.scss',changeDetection:ChangeDetectionStrategy.OnPush})
export class TransactionDetailsPage{private readonly id=inject(ActivatedRoute).snapshot.paramMap.get('transactionId');protected readonly transaction=toSignal(inject(BANKING_API).getTransactions().pipe(map(items=>items.find(item=>item.id===this.id))));}
