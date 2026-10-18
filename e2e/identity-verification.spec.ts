import { expect, test, type Page } from '@playwright/test';

async function openVerification(page: Page): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Customer ID or username').fill('demo-customer');
  await page.getByLabel('Password').fill('NovaraDemo1!');
  await page.getByRole('button', { name: 'Sign in to demo' }).click();
  await expect(page).toHaveURL(/auth\/mfa/);
}

for (const viewport of [{ width: 1440, height: 900 }, { width: 1024, height: 768 }, { width: 390, height: 844 }]) {
  test(`identity verification layout at ${viewport.width}px`, async ({ page }) => {
    await page.setViewportSize(viewport);
    await openVerification(page);
    await expect(page.getByText('NOVARA BANK', { exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Verify your identity' })).toBeVisible();
    await expect(page.getByLabel('Verification code')).toBeVisible();
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
    expect(overflow).toBe(false);
  });
}

test('validates and completes the supported OTP challenge', async ({ page }) => {
  await openVerification(page);
  await page.getByRole('button', { name: 'Verify and continue' }).click();
  await expect(page.getByRole('alert')).toContainText('complete 6-digit');
  await page.getByLabel('Verification code').fill('123456');
  await page.getByRole('button', { name: 'Verify and continue' }).click();
  await expect(page.getByRole('button', { name: 'Verifying…' })).toBeDisabled();
  await expect(page).toHaveURL(/dashboard/);
});
