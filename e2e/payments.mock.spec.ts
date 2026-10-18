import { expect, test } from '@playwright/test';

test.describe('payments UI with development adapter', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: 'Explore the fictional customer demo' }).click();
    await expect(page).toHaveURL(/dashboard/);
  });

  test('navigates the payment workspace and exposes real user actions', async ({ page }) => {
    await page.getByRole('link', { name: 'Payments', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Move money with confidence' })).toBeVisible();
    await page.getByRole('link', { name: /Scheduled/ }).click();
    await expect(page.getByRole('heading', { name: 'Scheduled & recurring' })).toBeVisible();
    await page.getByRole('link', { name: 'Create schedule' }).click();
    await expect(page.getByRole('heading', { name: 'Plan a payment' })).toBeVisible();
  });

  test('shows transfer validation before any submission', async ({ page }) => {
    await page.getByRole('link', { name: 'Payments', exact: true }).click();
    await page.getByRole('link', { name: 'New transfer' }).click();
    await page.getByRole('button', { name: 'Review transfer' }).click();
    await expect(page.getByRole('alert')).toContainText(/valid amount|required payment details/i);
  });
});
