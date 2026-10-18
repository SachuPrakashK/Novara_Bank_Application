import { describe, expect, it } from 'vitest';
import { parseInrInput } from './money-input';

describe('parseInrInput', () => {
  it('converts decimal text to integer minor units', () => { expect(parseInrInput('1,234.50')).toEqual({ amountMinor: 123450, currency: 'INR' }); });
  it('pads a single paise digit without floating-point arithmetic', () => { expect(parseInrInput('10.5')?.amountMinor).toBe(1050); });
  it('rejects zero, negative, and excess decimal precision', () => { expect(parseInrInput('0')).toBeNull(); expect(parseInrInput('-10')).toBeNull(); expect(parseInrInput('1.001')).toBeNull(); });
});
