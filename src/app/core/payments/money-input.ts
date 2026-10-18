import type { Money } from '../models/banking.models';

export function parseInrInput(value: string): Money | null {
  const normalized = value.trim().replaceAll(',', '');
  if (!/^(?:0|[1-9]\d{0,9})(?:\.\d{1,2})?$/.test(normalized)) return null;
  const [rupees, paise = ''] = normalized.split('.');
  const amountMinor = Number(`${rupees}${paise.padEnd(2, '0')}`);
  if (!Number.isSafeInteger(amountMinor) || amountMinor <= 0) return null;
  return { amountMinor, currency: 'INR' };
}
