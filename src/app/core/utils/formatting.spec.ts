import { describe, expect, it } from 'vitest';
import { formatMoney, maskAccountNumber } from './formatting';
describe('financial formatting', () => {
  it('formats integer minor units', () => { expect(formatMoney({ amountMinor: 123456, currency: 'INR' })).toContain('1,234.56'); });
  it('reveals only the last four account digits', () => { expect(maskAccountNumber('1234 5678 9012')).toBe('•••• 9012'); });
});
