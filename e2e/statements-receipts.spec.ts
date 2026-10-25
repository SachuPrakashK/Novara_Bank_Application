import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await page.getByRole('button', { name: 'Explore the fictional customer demo' }).click();
  await page.getByRole('link', { name: 'Statements' }).click();
  await expect(page.getByRole('heading', { name: 'Statements & Receipts' })).toBeVisible();
});

test('filters, sorts, pages, and opens a posted receipt', async ({ page }) => {
  await page.getByLabel('Direction').selectOption('DEBIT');
  await page.getByLabel('Type').selectOption('TRANSFER');
  await page.getByLabel('Sort').selectOption('oldest');
  await page.getByRole('searchbox', { name: 'Search' }).fill('Internal');
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await expect(page.getByText('Opening balance')).toBeVisible();
  await page.getByRole('button', { name: 'View receipt' }).click();
  await expect(page.getByRole('heading', { name: 'Transaction receipt' })).toBeVisible();
  await expect(page.getByRole('dialog').getByText('NVR-DEMO-1001')).toBeVisible();
  await page.getByRole('dialog').getByRole('button', { name: 'Close', exact: true }).click();
});

test('date preset remains responsive without horizontal page overflow', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.getByRole('button', { name: 'Last 3 months' }).click();
  await expect(page.getByText('Closing balance')).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false);
});
