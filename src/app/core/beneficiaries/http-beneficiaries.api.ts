import { HttpClient,HttpParams } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';
import type { BeneficiariesApi } from './beneficiaries-api';
import type { Beneficiary,BeneficiaryFilters,CreateBeneficiaryCommand,UpdateBeneficiaryCommand } from './beneficiary.models';
@Injectable() export class HttpBeneficiariesApi implements BeneficiariesApi {
  private readonly http=inject(HttpClient); private readonly url=`${inject(NOVARA_CONFIG).apiBaseUrl}/beneficiaries`;
  list(filters:BeneficiaryFilters={}):Observable<readonly Beneficiary[]>{let params=new HttpParams();if(filters.query)params=params.set('query',filters.query);if(filters.status)params=params.set('status',filters.status);if(filters.favorite!==undefined)params=params.set('favorite',filters.favorite);return this.http.get<readonly Beneficiary[]>(this.url,{params});}
  get(id:string):Observable<Beneficiary>{return this.http.get<Beneficiary>(`${this.url}/${encodeURIComponent(id)}`);}
  create(command:CreateBeneficiaryCommand,idempotencyKey:string):Observable<Beneficiary>{return this.http.post<Beneficiary>(this.url,command,{headers:{'Idempotency-Key':idempotencyKey}});}
  update(id:string,command:UpdateBeneficiaryCommand):Observable<Beneficiary>{return this.http.patch<Beneficiary>(`${this.url}/${encodeURIComponent(id)}`,command);}
  setFavorite(id:string,favorite:boolean):Observable<Beneficiary>{return this.http.put<Beneficiary>(`${this.url}/${encodeURIComponent(id)}/favorite`,{favorite});}
  deactivate(id:string):Observable<void>{return this.http.delete<void>(`${this.url}/${encodeURIComponent(id)}`);}
}
