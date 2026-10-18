import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController,provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe,expect,it } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { HttpBeneficiariesApi } from './http-beneficiaries.api';

describe('HttpBeneficiariesApi',()=>{
  function setup(){TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting(),{provide:NOVARA_CONFIG,useValue:{apiBaseUrl:'/api'}},HttpBeneficiariesApi]});return{api:TestBed.inject(HttpBeneficiariesApi),http:TestBed.inject(HttpTestingController)};}
  it('sends an idempotency key without duplicating the confirmation field',()=>{const{api,http}=setup();api.create({name:'Maya Rao',nickname:'Home',bankName:'Novara Bank',accountNumber:'123456789012',routingCode:'NOVA0001',transferMethod:'INTERNAL'},'beneficiary_key_1234').subscribe();const request=http.expectOne('/api/beneficiaries');expect(request.request.method).toBe('POST');expect(request.request.headers.get('Idempotency-Key')).toBe('beneficiary_key_1234');expect(request.request.body).not.toHaveProperty('confirmAccountNumber');request.flush({});http.verify();});
  it('encodes filters and uses ownership-scoped resource routes',()=>{const{api,http}=setup();api.list({query:'Maya Rao',status:'ACTIVE',favorite:true}).subscribe();const request=http.expectOne(value=>value.url==='/api/beneficiaries');expect(request.request.params.get('query')).toBe('Maya Rao');expect(request.request.params.get('status')).toBe('ACTIVE');expect(request.request.params.get('favorite')).toBe('true');request.flush([]);http.verify();});
});
