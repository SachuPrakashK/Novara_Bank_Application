import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, type Observable } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';
import type { PaymentsApi } from './payments-api';
import type { CreateBeneficiaryCommand, CreateScheduleCommand, PaymentBeneficiary, PaymentDraft, PaymentLimits, PaymentPage, PaymentQuote, PaymentResult, PaymentSchedule, SubmitPaymentCommand } from './payment.models';

@Injectable() export class HttpPaymentsApi implements PaymentsApi {
  private readonly http=inject(HttpClient);private readonly base=inject(NOVARA_CONFIG).apiBaseUrl;
  getBeneficiaries():Observable<readonly PaymentBeneficiary[]>{return this.http.get<readonly ApiBeneficiary[]>(`${this.base}/beneficiaries`).pipe(map(rows=>rows.map(b=>({id:b.id,name:b.displayName,bankName:b.bankName,maskedAccount:b.maskedAccount,favorite:b.favorite,status:b.status==='ACTIVE'&&(!b.availableFrom||new Date(b.availableFrom)<=new Date())?'ACTIVE':b.status==='ACTIVE'?'COOLING_PERIOD':'INACTIVE',availableFrom:b.availableFrom}))));}
  createBeneficiary(command:CreateBeneficiaryCommand):Observable<PaymentBeneficiary>{return this.http.post<PaymentBeneficiary>(`${this.base}/beneficiaries`,command,{headers:{'Idempotency-Key':crypto.randomUUID().replaceAll('-','')}});}
  createQuote(d:PaymentDraft):Observable<PaymentQuote>{return this.http.post<ApiPayment>(`${this.base}/payments/intents`,{sourceAccountId:d.sourceAccountId,destinationType:d.destinationType??'BENEFICIARY',destinationId:d.beneficiaryId,amountMinor:d.amount.amountMinor,currency:d.amount.currency,method:d.method,description:d.note}).pipe(map(toQuote));}
  submitPayment(c:SubmitPaymentCommand):Observable<PaymentResult>{return this.http.post<ApiPayment>(`${this.base}/payments/${encodeURIComponent(c.quoteId)}/confirm`,{},{headers:{'Idempotency-Key':c.idempotencyKey}}).pipe(map(p=>toResult(p,c.idempotencyKey)));}
  getPaymentStatus(id:string):Observable<PaymentResult>{return this.http.get<ApiPayment>(`${this.base}/payments/${encodeURIComponent(id)}`).pipe(map(p=>toResult(p,id)));}
  getPaymentStatusByIdempotencyKey(key:string):Observable<PaymentResult>{return this.http.get<ApiPayment>(`${this.base}/transfers/by-idempotency-key/${encodeURIComponent(key)}`).pipe(map(t=>toResult(t,key)));}
  getPayments(query='',status='',page=0):Observable<PaymentPage>{return this.http.get<ApiPage>(`${this.base}/payments`,{params:{query,status,page}}).pipe(map(p=>({...p,items:p.items.map(toQuote)})));}
  getPayment(id:string):Observable<PaymentQuote>{return this.http.get<ApiPayment>(`${this.base}/payments/${encodeURIComponent(id)}`).pipe(map(toQuote));}
  getLimits():Observable<PaymentLimits>{return this.http.get<PaymentLimits>(`${this.base}/payment-limits`);}
  lowerDailyLimit(amountMinor:number):Observable<PaymentLimits>{return this.http.patch<PaymentLimits>(`${this.base}/payment-limits/customer-preferences`,{dailyLimitMinor:amountMinor});}
  getSchedules():Observable<readonly PaymentSchedule[]>{return this.http.get<readonly PaymentSchedule[]>(`${this.base}/payment-schedules`);}
  createSchedule(command:CreateScheduleCommand):Observable<PaymentSchedule>{return this.http.post<PaymentSchedule>(`${this.base}/payment-schedules`,command);}
  cancelSchedule(id:string):Observable<PaymentSchedule>{return this.http.post<PaymentSchedule>(`${this.base}/payment-schedules/${encodeURIComponent(id)}/cancel`,{});}
}
interface ApiBeneficiary{readonly id:string;readonly displayName:string;readonly bankName:string;readonly maskedAccount:string;readonly favorite:boolean;readonly status:string;readonly availableFrom?:string;}
interface ApiPayment{readonly id:string;readonly status:PaymentQuote['status'];readonly destinationType:'OWN_ACCOUNT'|'BENEFICIARY';readonly sourceAccountId:string;readonly sourceLabel:string;readonly sourceMaskedNumber:string;readonly destinationId:string;readonly destinationName:string;readonly destinationMaskedNumber:string;readonly amountMinor:number;readonly feeMinor:number;readonly totalDebitMinor:number;readonly currency:'INR';readonly method:'INTERNAL';readonly description?:string;readonly requiresStepUp:boolean;readonly reference?:string;readonly createdAt:string;readonly expiresAt:string;readonly completedAt?:string;readonly timeline:PaymentQuote['timeline'];}
interface ApiPage{readonly items:readonly ApiPayment[];readonly page:number;readonly size:number;readonly total:number;}
function toQuote(p:ApiPayment):PaymentQuote{return{quoteId:p.id,sourceAccountId:p.sourceAccountId,sourceLabel:p.sourceLabel,sourceMaskedNumber:p.sourceMaskedNumber,beneficiaryId:p.destinationId,beneficiaryName:p.destinationName,beneficiaryMaskedNumber:p.destinationMaskedNumber,amount:{amountMinor:p.amountMinor,currency:p.currency},fee:{amountMinor:p.feeMinor,currency:p.currency},totalDebit:{amountMinor:p.totalDebitMinor,currency:p.currency},method:p.method,processingMessage:'Novara internal transfer',expiresAt:p.expiresAt,note:p.description,status:p.status,destinationType:p.destinationType,requiresStepUp:p.requiresStepUp,createdAt:p.createdAt,completedAt:p.completedAt,reference:p.reference,timeline:p.timeline};}
function toResult(p:ApiPayment,key:string):PaymentResult{return{outcome:p.status==='PENDING_AUTHORIZATION'?'PROCESSING':p.status as PaymentResult['outcome'],clientRequestId:key,paymentId:p.id,reference:p.reference,submittedAt:p.createdAt,message:p.status==='COMPLETED'?'Transfer completed.':'Payment status: '+p.status?.toLowerCase().replaceAll('_',' ')};}
