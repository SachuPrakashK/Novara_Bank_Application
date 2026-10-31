import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { NOVARA_CONFIG } from '../config/app-config';
import { of } from 'rxjs';

export interface Profile {
  fullName: string;
  preferredDisplayName: string | null;
  registeredEmail: string | null;
  emailVerified: boolean;
  registeredMobile: string | null;
  mobileVerified: boolean;
  address: string | null;
  customerSince: string;
  version: number;
  linkedAccountCount: number;
}
export interface AppPreferences {
  language: string;
  theme: 'LIGHT' | 'DARK';
  dateFormat: string;
  reducedMotion: boolean;
  version: number;
}
export interface ServiceRequest {
  id: string;
  category: string;
  description: string;
  status: string;
  response: string | null;
  createdAt: string;
  updatedAt: string;
}
export interface NotificationItem {
  id: string;
  type: string;
  title: string;
  message: string;
  navigationTarget: string | null;
  readAt: string | null;
  createdAt: string;
}
export interface NotificationPreferences {
  transactionActivity: boolean;
  statements: boolean;
  deposits: boolean;
  loanApplications: boolean;
  serviceRequests: boolean;
  version: number;
  mandatorySecurityAlerts: boolean;
}
export interface SecuritySummary {
  authenticationStatus: string;
  mfaStatus: string;
  lastSuccessfulLogin: string | null;
  sessionCreatedAt: string | null;
  sessionExpiresAt: string | null;
  identityManagementLinkAvailable: boolean;
  identityManagementMessage: string;
}
export interface BrowserSession {
  id: string;
  createdAt: string;
  lastAccessedAt: string;
  expiresAt: string;
  current: boolean;
}
export interface SecurityEvent {
  action: string;
  outcome: string;
  occurredAt: string;
}
export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class SelfServiceApi {
  private readonly http = inject(HttpClient);
  private readonly config = inject(NOVARA_CONFIG);
  private readonly base = this.config.apiBaseUrl;
  private demoProfile: Profile = {
    fullName: 'Aarav Kapoor',
    preferredDisplayName: 'Aarav',
    registeredEmail: 'a••••@example.test',
    emailVerified: true,
    registeredMobile: '••••••0042',
    mobileVerified: true,
    address: null,
    customerSince: '2024-01-15T00:00:00Z',
    version: 0,
    linkedAccountCount: 2,
  };
  private demoPreferences: AppPreferences = {
    language: 'en',
    theme: 'DARK',
    dateFormat: 'DD_MMM_YYYY',
    reducedMotion: false,
    version: 0,
  };
  private demoNotificationPreferences: NotificationPreferences = {
    transactionActivity: true,
    statements: true,
    deposits: true,
    loanApplications: true,
    serviceRequests: true,
    version: 0,
    mandatorySecurityAlerts: true,
  };
  private demoRequests: ServiceRequest[] = [];
  private demoNotifications: NotificationItem[] = [];
  profile() {
    return this.config.useMockApi
      ? of(this.demoProfile)
      : this.http.get<Profile>(`${this.base}/profile`);
  }
  updateProfile(body: { preferredDisplayName: string; version: number }) {
    if (!this.config.useMockApi) return this.http.patch<Profile>(`${this.base}/profile`, body);
    this.demoProfile = {
      ...this.demoProfile,
      preferredDisplayName: body.preferredDisplayName,
      version: this.demoProfile.version + 1,
    };
    return of(this.demoProfile);
  }
  preferences() {
    return this.config.useMockApi
      ? of(this.demoPreferences)
      : this.http.get<AppPreferences>(`${this.base}/profile/preferences`);
  }
  updatePreferences(body: AppPreferences) {
    if (!this.config.useMockApi)
      return this.http.put<AppPreferences>(`${this.base}/profile/preferences`, body);
    this.demoPreferences = { ...body, version: body.version + 1 };
    return of(this.demoPreferences);
  }
  requests(page = 0, size = 20, search = '', sort = 'newest') {
    if (!this.config.useMockApi)
      return this.http.get<Page<ServiceRequest>>(`${this.base}/service-requests`, {
        params: { page, size, search, sort },
      });
    const ordered = [...this.demoRequests].filter((v) =>
      `${v.category} ${v.description}`.toLowerCase().includes(search.toLowerCase()),
    );
    if (sort === 'oldest') ordered.reverse();
    return of(this.page(ordered, page, size));
  }
  createRequest(category: string, description: string) {
    if (!this.config.useMockApi)
      return this.http.post<ServiceRequest>(
        `${this.base}/service-requests`,
        { category, description },
        { headers: new HttpHeaders({ 'Idempotency-Key': crypto.randomUUID() }) },
      );
    const now = new Date().toISOString();
    const value = {
      id: crypto.randomUUID(),
      category,
      description,
      status: 'SUBMITTED',
      response: null,
      createdAt: now,
      updatedAt: now,
    };
    this.demoRequests.unshift(value);
    this.demoNotifications.unshift({
      id: crypto.randomUUID(),
      type: 'SERVICE_REQUEST',
      title: 'Service request submitted',
      message: 'Your service request has been received.',
      navigationTarget: null,
      readAt: null,
      createdAt: now,
    });
    return of(value);
  }
  notifications(page = 0, size = 25) {
    return this.config.useMockApi
      ? of(this.page(this.demoNotifications, page, size))
      : this.http.get<Page<NotificationItem>>(`${this.base}/notifications`, {
          params: { page, size },
        });
  }
  readNotification(id: string) {
    if (!this.config.useMockApi)
      return this.http.post<void>(`${this.base}/notifications/${encodeURIComponent(id)}/read`, {});
    this.demoNotifications = this.demoNotifications.map((v) =>
      v.id === id ? { ...v, readAt: new Date().toISOString() } : v,
    );
    return of(undefined);
  }
  readAll() {
    if (!this.config.useMockApi)
      return this.http.post<void>(`${this.base}/notifications/read-all`, {});
    this.demoNotifications = this.demoNotifications.map((v) => ({
      ...v,
      readAt: v.readAt ?? new Date().toISOString(),
    }));
    return of(undefined);
  }
  notificationPreferences() {
    return this.config.useMockApi
      ? of(this.demoNotificationPreferences)
      : this.http.get<NotificationPreferences>(`${this.base}/notifications/preferences`);
  }
  updateNotificationPreferences(body: NotificationPreferences) {
    if (!this.config.useMockApi)
      return this.http.put<NotificationPreferences>(`${this.base}/notifications/preferences`, body);
    this.demoNotificationPreferences = { ...body, version: body.version + 1 };
    return of(this.demoNotificationPreferences);
  }
  security() {
    return this.config.useMockApi
      ? of<SecuritySummary>({
          authenticationStatus: 'AUTHENTICATED',
          mfaStatus: 'SANDBOX_DEMO_NOT_PROVIDER_VERIFIED',
          lastSuccessfulLogin: null,
          sessionCreatedAt: new Date().toISOString(),
          sessionExpiresAt: new Date(Date.now() + 900000).toISOString(),
          identityManagementLinkAvailable: false,
          identityManagementMessage:
            'Sandbox demonstration. Identity management requires the configured provider.',
        })
      : this.http.get<SecuritySummary>(`${this.base}/security/summary`);
  }
  securitySessions() {
    const now = new Date();
    return this.config.useMockApi
      ? of<BrowserSession[]>([
          {
            id: 'sandbox-current',
            createdAt: now.toISOString(),
            lastAccessedAt: now.toISOString(),
            expiresAt: new Date(now.getTime() + 900000).toISOString(),
            current: true,
          },
        ])
      : this.http.get<BrowserSession[]>(`${this.base}/security/sessions`);
  }
  securityEvents() {
    return this.config.useMockApi
      ? of<SecurityEvent[]>([])
      : this.http.get<SecurityEvent[]>(`${this.base}/security/events`);
  }
  terminateSession(id: string) {
    return this.config.useMockApi
      ? of(undefined)
      : this.http.delete<void>(`${this.base}/security/sessions/${encodeURIComponent(id)}`);
  }
  private page<T>(all: T[], page: number, size: number): Page<T> {
    return {
      items: all.slice(page * size, (page + 1) * size),
      page,
      size,
      totalElements: all.length,
      totalPages: Math.ceil(all.length / size),
    };
  }
}
