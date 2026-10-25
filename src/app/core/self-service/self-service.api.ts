import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { NOVARA_CONFIG } from '../config/app-config';

export interface Profile { fullName:string; preferredDisplayName:string|null; registeredEmail:string|null; emailVerified:boolean; registeredMobile:string|null; mobileVerified:boolean; address:string|null; customerSince:string; version:number; linkedAccountCount:number; }
export interface AppPreferences { language:string; theme:string; dateFormat:string; reducedMotion:boolean; version:number; }
export interface ServiceRequest { id:string; category:string; description:string; status:string; response:string|null; createdAt:string; updatedAt:string; }
export interface NotificationItem { id:string; type:string; title:string; message:string; navigationTarget:string|null; readAt:string|null; createdAt:string; }
export interface NotificationPreferences { transactionActivity:boolean; statements:boolean; deposits:boolean; loanApplications:boolean; serviceRequests:boolean; version:number; mandatorySecurityAlerts:boolean; }
export interface SecuritySummary { authenticationStatus:string; mfaStatus:string; lastSuccessfulLogin:string|null; sessionCreatedAt:string|null; sessionExpiresAt:string|null; identityManagementLinkAvailable:boolean; identityManagementMessage:string; }

@Injectable({providedIn:'root'}) export class SelfServiceApi {
 private readonly http=inject(HttpClient); private readonly base=inject(NOVARA_CONFIG).apiBaseUrl;
 profile(){return this.http.get<Profile>(`${this.base}/profile`);} updateProfile(body:{preferredDisplayName:string;version:number}){return this.http.patch<Profile>(`${this.base}/profile`,body);}
 preferences(){return this.http.get<AppPreferences>(`${this.base}/profile/preferences`);} updatePreferences(body:AppPreferences){return this.http.put<AppPreferences>(`${this.base}/profile/preferences`,body);}
 requests(){return this.http.get<ServiceRequest[]>(`${this.base}/service-requests`);} createRequest(category:string,description:string){return this.http.post<ServiceRequest>(`${this.base}/service-requests`,{category,description},{headers:new HttpHeaders({'Idempotency-Key':crypto.randomUUID()})});}
 notifications(){return this.http.get<NotificationItem[]>(`${this.base}/notifications`);} readNotification(id:string){return this.http.post<void>(`${this.base}/notifications/${encodeURIComponent(id)}/read`,{});} readAll(){return this.http.post<void>(`${this.base}/notifications/read-all`,{});}
 notificationPreferences(){return this.http.get<NotificationPreferences>(`${this.base}/notifications/preferences`);} updateNotificationPreferences(body:NotificationPreferences){return this.http.put<NotificationPreferences>(`${this.base}/notifications/preferences`,body);}
 security(){return this.http.get<SecuritySummary>(`${this.base}/security/summary`);}
}
