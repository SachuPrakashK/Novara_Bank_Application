import { AsyncPipe,DatePipe } from '@angular/common';
import { ChangeDetectionStrategy,Component,inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BehaviorSubject,switchMap } from 'rxjs';
import { PAYMENTS_API } from '../../core/payments/payments-api';
import { IconComponent } from '../../shared/ui/icon.component';
import { MoneyComponent } from '../../shared/ui/money.component';

@Component({selector:'app-payment-schedules',imports:[AsyncPipe,DatePipe,RouterLink,IconComponent,MoneyComponent],template:`
<header class="premium-header"><div><span class="eyebrow">PLANNED PAYMENTS</span><h1>Scheduled & recurring</h1><p>Durable instructions executed by Novara services—not browser timers.</p></div><a class="button primary" routerLink="/payments/schedules/new">Create schedule</a></header>
<section class="content-card"><div class="section-heading"><div><span class="eyebrow">UPCOMING</span><h2>Your payment plans</h2></div></div>
@if(schedules$|async;as schedules){@if(!schedules.length){<div class="state"><novara-icon name="calendar"/><h3>Nothing scheduled</h3><p>Choose a date or recurrence for your next payment plan.</p></div>}@else{@for(s of schedules;track s.id){<article class="schedule-card"><span class="type-icon"><novara-icon [name]="s.recurrence==='ONCE'?'calendar':'repeat'"/></span><div><span class="badge">{{s.status}}</span><h3>{{s.destinationName}}</h3><p>{{s.recurrence}} · Next {{s.nextExecutionAt|date:'medium'}} · {{s.timezone}}</p><small>{{s.sourceMaskedNumber}} → {{s.destinationMaskedNumber}}</small></div><div><novara-money [minor]="s.amountMinor" currency="INR"/>@if(s.status==='ACTIVE'){<button class="button secondary" (click)="cancel(s.id)">Cancel</button>}</div></article>}}}</section>
<aside class="security-strip"><novara-icon name="shield"/><div><b>Revalidated at execution</b><span>Funds, account eligibility, recipient status and limits are checked again. High-risk occurrences require review.</span></div></aside>`,styleUrl:'./payments-premium.scss',changeDetection:ChangeDetectionStrategy.OnPush})
export class PaymentSchedulesPage{private api=inject(PAYMENTS_API);private reload=new BehaviorSubject(0);protected schedules$=this.reload.pipe(switchMap(()=>this.api.getSchedules()));protected cancel(id:string){this.api.cancelSchedule(id).subscribe(()=>this.reload.next(this.reload.value+1));}}
