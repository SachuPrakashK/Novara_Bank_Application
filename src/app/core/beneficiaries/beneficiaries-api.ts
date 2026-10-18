import { InjectionToken } from '@angular/core';
import type { Observable } from 'rxjs';
import type { Beneficiary, BeneficiaryFilters, CreateBeneficiaryCommand, UpdateBeneficiaryCommand } from './beneficiary.models';
export interface BeneficiariesApi {
  list(filters?:BeneficiaryFilters):Observable<readonly Beneficiary[]>; get(id:string):Observable<Beneficiary>;
  create(command:CreateBeneficiaryCommand,idempotencyKey:string):Observable<Beneficiary>; update(id:string,command:UpdateBeneficiaryCommand):Observable<Beneficiary>;
  setFavorite(id:string,favorite:boolean):Observable<Beneficiary>; deactivate(id:string):Observable<void>;
}
export const BENEFICIARIES_API=new InjectionToken<BeneficiariesApi>('BENEFICIARIES_API');
