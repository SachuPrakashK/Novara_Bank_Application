import { InjectionToken } from '@angular/core';import type { Observable } from 'rxjs';import type { LoanApplication,LoanProduct,LoanQuote } from './loan.models';
export interface LoansApi{products():Observable<readonly LoanProduct[]>;applications():Observable<readonly LoanApplication[]>;quote(productId:string,principalMinor:number,tenureMonths:number):Observable<LoanQuote>;apply(quoteId:string,purposeCategory:string):Observable<LoanApplication>}
export const LOANS_API=new InjectionToken<LoansApi>('LOANS_API');
