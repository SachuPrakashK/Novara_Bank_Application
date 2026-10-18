import { Injectable } from '@angular/core';
import { delay,of,type Observable } from 'rxjs';
import type { BeneficiariesApi } from './beneficiaries-api';
import type { Beneficiary,BeneficiaryFilters,CreateBeneficiaryCommand,UpdateBeneficiaryCommand } from './beneficiary.models';
@Injectable() export class MockBeneficiariesApi implements BeneficiariesApi {
  private items:Beneficiary[]=[this.item('ben-001','Maya Rao','Home','Novara Bank','•••• 3491','NOVA0001','ACTIVE',true),this.item('ben-002','Arjun Mehta',undefined,'Horizon Bank','•••• 7082','HZN00012','ACTIVE',false),this.item('ben-003','Aster Homes','Rent','Unity Bank','•••• 1260','UTY00018','PENDING_VERIFICATION',false)];
  list(filters:BeneficiaryFilters={}):Observable<readonly Beneficiary[]>{const query=filters.query?.trim().toLowerCase();return of(this.items.filter(item=>(!query||`${item.name} ${item.nickname??''} ${item.bankName}`.toLowerCase().includes(query))&&(!filters.status||item.status===filters.status)&&(filters.favorite===undefined||item.favorite===filters.favorite))).pipe(delay(180));}
  get(id:string):Observable<Beneficiary>{return of(this.require(id)).pipe(delay(100));}
  create(command:CreateBeneficiaryCommand,idempotencyKey:string):Observable<Beneficiary>{void idempotencyKey;const item=this.item(crypto.randomUUID(),command.name,command.nickname,command.bankName,`•••• ${command.accountNumber.slice(-4)}`,command.routingCode.toUpperCase(),'PENDING_VERIFICATION',false,command.transferMethod);this.items=[item,...this.items];return of(item).pipe(delay(350));}
  update(id:string,command:UpdateBeneficiaryCommand):Observable<Beneficiary>{return this.change(id,item=>({...item,...command,displayName:command.nickname?.trim()||command.name.trim(),updatedAt:new Date().toISOString()}));}
  setFavorite(id:string,favorite:boolean):Observable<Beneficiary>{return this.change(id,item=>({...item,favorite,updatedAt:new Date().toISOString()}));}
  deactivate(id:string):Observable<void>{this.items=this.items.map(item=>item.id===id?{...item,status:'DEACTIVATED',favorite:false,updatedAt:new Date().toISOString()}:item);return of(undefined).pipe(delay(200));}
  private change(id:string,mutate:(item:Beneficiary)=>Beneficiary):Observable<Beneficiary>{const changed=mutate(this.require(id));this.items=this.items.map(item=>item.id===id?changed:item);return of(changed).pipe(delay(180));}
  private require(id:string):Beneficiary{const item=this.items.find(candidate=>candidate.id===id);if(!item)throw new Error('Beneficiary not found');return item;}
  private item(id:string,name:string,nickname:string|undefined,bankName:string,maskedAccount:string,routingCode:string,status:Beneficiary['status'],favorite:boolean,transferMethod:Beneficiary['transferMethod']='INTERNAL'):Beneficiary{const now=new Date().toISOString();return{id,name,nickname,displayName:nickname||name,bankName,maskedAccount,routingCode,transferMethod,status,favorite,createdAt:now,updatedAt:now};}
}
