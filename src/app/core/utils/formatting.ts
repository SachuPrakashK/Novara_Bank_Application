import type { Money } from '../models/banking.models';
export function formatMoney(money: Money, locale = 'en-IN'): string { return new Intl.NumberFormat(locale, { style: 'currency', currency: money.currency, minimumFractionDigits: 2 }).format(money.amountMinor / 100); }
export function maskAccountNumber(value: string): string { const digits = value.replace(/\D/g, ''); return digits.length < 4 ? '••••' : `•••• ${digits.slice(-4)}`; }
