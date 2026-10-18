import { expect, test, type Page } from '@playwright/test';

async function openDashboard(page: Page): Promise<void> {
  await page.goto('/login');
  await page.getByRole('button', { name: 'Explore the fictional customer demo' }).click();
  await expect(page).toHaveURL(/dashboard/);
}

test('uses Poppins outside the authentication brand panel', async ({ page }) => {
  await page.goto('/login');
  const rhsFont = await page.locator('.auth-panel').evaluate((element) => getComputedStyle(element).fontFamily);
  const lhsFont = await page.locator('.login-story').evaluate((element) => getComputedStyle(element).fontFamily);
  expect(rhsFont).toContain('Poppins');
  expect(lhsFont).not.toContain('Poppins');
});

test('collapses, navigates, and expands the desktop sidebar', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  await openDashboard(page);
  const sidebar = page.locator('.sidebar');
  const expandedWidth = await sidebar.evaluate((element) => element.getBoundingClientRect().width);
  await page.getByRole('button', { name: 'Collapse sidebar' }).click();
  await expect(page.getByRole('button', { name: 'Expand sidebar' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Accounts' })).toHaveAttribute('title', 'Accounts');
  const collapsedWidth = await sidebar.evaluate((element) => element.getBoundingClientRect().width);
  expect(collapsedWidth).toBeLessThan(expandedWidth);
  await page.getByRole('link', { name: 'Accounts' }).click();
  await expect(page).toHaveURL(/accounts/);
  await expect(page.locator('.shell')).toHaveClass(/sidebar-collapsed/);
  await page.getByRole('button', { name: 'Expand sidebar' }).click();
  await expect(page.getByRole('button', { name: 'Collapse sidebar' })).toBeVisible();
});

test('uses a dismissible navigation drawer on mobile', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await openDashboard(page);
  await page.getByRole('button', { name: 'Open navigation' }).click();
  await expect(page.locator('.sidebar')).toHaveClass(/open/);
  await page.keyboard.press('Escape');
  await expect(page.locator('.sidebar')).not.toHaveClass(/open/);
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  expect(overflow).toBe(false);
});
