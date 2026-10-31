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
  const collapseControl = page.getByRole('button', { name: 'Collapse sidebar' });
  const expandedControlBox = await collapseControl.boundingBox();
  expect(expandedControlBox).not.toBeNull();
  expect(expandedControlBox!.width).toBeCloseTo(expandedControlBox!.height, 0);
  expect(expandedControlBox!.width).toBeLessThan(50);
  const expandedWidth = await sidebar.evaluate((element) => element.getBoundingClientRect().width);
  await collapseControl.click();
  const expandControl = page.getByRole('button', { name: 'Expand sidebar' });
  await expect(expandControl).toBeVisible();
  const collapsedControlBox = await expandControl.boundingBox();
  expect(collapsedControlBox).not.toBeNull();
  expect(collapsedControlBox!.width).toBeCloseTo(collapsedControlBox!.height, 0);
  expect(collapsedControlBox!.width).toBeLessThan(50);
  await expect(page.getByRole('link', { name: 'Accounts' })).toHaveAttribute('title', 'Accounts');
  const collapsedWidth = await sidebar.evaluate((element) => element.getBoundingClientRect().width);
  expect(collapsedWidth).toBeLessThan(expandedWidth);
  await page.getByRole('link', { name: 'Accounts' }).click();
  await expect(page).toHaveURL(/accounts/);
  await expect(page.locator('.shell')).toHaveClass(/sidebar-collapsed/);
  await expandControl.click();
  await expect(page.getByRole('button', { name: 'Collapse sidebar' })).toBeVisible();
});

test('keeps sidebar branding and footer fixed while navigation scrolls', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 520 });
  await openDashboard(page);

  const sidebar = page.locator('.sidebar');
  const brand = sidebar.locator('.brand');
  const navigation = sidebar.locator('nav');
  const footer = sidebar.locator('.sidebar-foot');
  const metrics = await navigation.evaluate((element) => ({
    clientHeight: element.clientHeight,
    scrollHeight: element.scrollHeight,
    overflowY: getComputedStyle(element).overflowY,
  }));
  expect(metrics.overflowY).toBe('auto');
  expect(metrics.scrollHeight).toBeGreaterThan(metrics.clientHeight);

  const brandTop = (await brand.boundingBox())!.y;
  const footerBottom = (await footer.boundingBox())!.y + (await footer.boundingBox())!.height;
  await navigation.evaluate((element) => (element.scrollTop = element.scrollHeight));
  expect((await brand.boundingBox())!.y).toBeCloseTo(brandTop, 0);
  expect((await footer.boundingBox())!.y + (await footer.boundingBox())!.height).toBeCloseTo(
    footerBottom,
    0,
  );
});

test('uses a dismissible navigation drawer on mobile', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await openDashboard(page);
  await page.getByRole('button', { name: 'Open navigation' }).click();
  await expect(page.locator('.sidebar')).toHaveClass(/open/);
  await expect(page.locator('.sidebar')).toBeFocused();
  expect(await page.locator('body').evaluate((element) => getComputedStyle(element).overflow)).toBe(
    'hidden',
  );
  await page.keyboard.press('Escape');
  await expect(page.locator('.sidebar')).not.toHaveClass(/open/);
  await expect(page.getByRole('button', { name: 'Open navigation' })).toBeFocused();
  expect(await page.locator('body').evaluate((element) => element.style.overflow)).toBe('');
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  expect(overflow).toBe(false);
});
