import {InjectionToken} from '@angular/core';import type{Observable}from'rxjs';import type{ReceiptDetails,StatementAccount,StatementGeneration,StatementPage}from'./statement.models';
export interface StatementQuery{accountId:string;from:string;to:string;direction?:string;type?:string;search?:string;sort?:string;page:number;size:number}
export interface StatementsApi{accounts():Observable<readonly StatementAccount[]>;transactions(query:StatementQuery):Observable<StatementPage>;generate(accountId:string,from:string,to:string):Observable<StatementGeneration>;download(id:string,format:'pdf'|'csv'):Observable<Blob>;receiptDetails(id:string):Observable<ReceiptDetails>;receipt(id:string):Observable<Blob>}
export const STATEMENTS_API=new InjectionToken<StatementsApi>('STATEMENTS_API');
