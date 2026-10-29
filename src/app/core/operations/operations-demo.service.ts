import { Injectable } from '@angular/core';
export interface DemoRequest{id:string;reference:string;category:string;status:string;customer:string;createdAt:string;updatedAt:string;description:string;}
export interface DemoCustomer{id:string;reference:string;name:string;email:string;mobile:string;status:string;accounts:number;since:string;}
export interface DemoAccount{id:string;reference:string;type:string;currency:string;status:string;balance:string;lastActivity:string;}
export interface DemoAudit{id:string;action:string;actorCategory:string;outcome:string;correlation:string;occurredAt:string;}
@Injectable({providedIn:'root'})export class OperationsDemoService{
 readonly requests:DemoRequest[]=Array.from({length:23},(_,i)=>({id:`request-${i+1}`,reference:`SR-${String(4100+i)}`,category:['ACCOUNT_INFORMATION','STATEMENT_ASSISTANCE','CARD_SUPPORT'][i%3],status:['SUBMITTED','UNDER_REVIEW','ADDITIONAL_INFORMATION_REQUIRED','RESOLVED'][i%4],customer:`Synthetic Customer ${i%8+1}`,createdAt:`2026-10-${String(25-i%10).padStart(2,'0')}T09:00:00Z`,updatedAt:`2026-10-${String(25-i%8).padStart(2,'0')}T12:00:00Z`,description:'Synthetic demonstration request. No real customer data.'}));
 readonly customers:DemoCustomer[]=Array.from({length:12},(_,i)=>({id:`customer-${i+1}`,reference:`CUST-${String(8000+i)}`,name:`Synthetic Customer ${i+1}`,email:`s••••${i+1}@example.test`,mobile:`••••••${String(1200+i).slice(-4)}`,status:i===9?'RESTRICTED':'ACTIVE',accounts:i%3+1,since:`202${i%5}-01-15`}));
 readonly accounts:DemoAccount[]=Array.from({length:18},(_,i)=>({id:`account-${i+1}`,reference:`•••• ${String(3000+i)}`,type:i%3===0?'CURRENT':'SAVINGS',currency:'INR',status:i===14?'RESTRICTED':'ACTIVE',balance:`₹${(125000+i*17350).toLocaleString('en-IN')}.00`,lastActivity:`2026-10-${String(25-i%8).padStart(2,'0')}`}));
 readonly audits:DemoAudit[]=Array.from({length:20},(_,i)=>({id:`audit-${i+1}`,action:['OIDC_LOGIN','SERVICE_REQUEST_VIEWED','SESSION_TERMINATED'][i%3],actorCategory:i%2?'SYNTHETIC_STAFF':'SYSTEM',outcome:'SUCCESS',correlation:`demo-corr-${100+i}`,occurredAt:`2026-10-${String(25-i%7).padStart(2,'0')}T10:00:00Z`}));
 readonly outbox={pending:7,processed:142,failed:null,oldestPending:'18 minutes',consumerHealth:'NOT REPRESENTED BY CURRENT OUTBOX'};
 page<T>(all:T[],page:number,size:number){return{items:all.slice(page*size,(page+1)*size),total:all.length,pages:Math.ceil(all.length/size)};}
}
