import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { OperationsDemoService } from './operations-demo.service';

describe('OperationsDemoService isolation', () => {
  it('constructs without an HTTP provider and exposes only synthetic fixtures', () => {
    TestBed.configureTestingModule({});
    const service = TestBed.inject(OperationsDemoService);
    expect(service.requests.length).toBeGreaterThan(0);
    expect(service.requests.every((request) => request.description.includes('Synthetic'))).toBe(true);
    expect(service.customers.every((customer) => customer.email.includes('example.test'))).toBe(true);
  });

  it('keeps customer and account identifiers masked or synthetic', () => {
    const service = new OperationsDemoService();
    expect(service.customers.every((customer) => customer.reference.startsWith('CUST-'))).toBe(true);
    expect(service.accounts.every((account) => !/\d{8,}/.test(account.reference))).toBe(true);
    expect(service.audits.every((event) => event.correlation.startsWith('demo-corr-'))).toBe(true);
  });
});
