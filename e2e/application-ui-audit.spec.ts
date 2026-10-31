import { expect, test, type Page } from '@playwright/test';

const customerRoutes = [
  '/dashboard',
  '/accounts',
  '/accounts/acc-savings-01',
  '/transactions',
  '/transactions/txn-001',
  '/payments',
  '/payments/transfer',
  '/payments/review',
  '/payments/result',
  '/payments/history',
  '/payments/schedules',
  '/payments/schedules/new',
  '/payments/limits',
  '/payments/demo-missing',
  '/beneficiaries',
  '/security',
  '/profile',
  '/account-services',
  '/notifications',
  '/preferences',
  '/cards',
  '/cards/card-demo-gold',
  '/deposits',
  '/loans',
  '/statements',
  '/investments',
  '/support',
] as const;

const operationsRoutes = [
  '/operations-demo/dashboard',
  '/operations-demo/requests',
  '/operations-demo/customers',
  '/operations-demo/accounts',
  '/operations-demo/audit',
  '/operations-demo/monitoring',
  '/operations-demo/settings',
] as const;

async function enterCustomerDemo(page: Page): Promise<void> {
  await page.goto('/login');
  await page.getByRole('button', { name: 'Explore the fictional customer demo' }).click();
  await expect(page).toHaveURL(/dashboard/);
}

async function navigateSpa(page: Page, route: string): Promise<void> {
  await page.evaluate((path) => {
    history.pushState({}, '', path);
    window.dispatchEvent(new PopStateEvent('popstate'));
  }, route);
  await expect(page).toHaveURL(new RegExp(`${route.replaceAll('/', '\\/')}$`));
}

async function assertRouteSurface(page: Page, route: string): Promise<void> {
  await navigateSpa(page, route);
  await expect(page.locator('body')).toBeVisible();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.waitForTimeout(550);
  const overflow = await page.evaluate(
    () => document.documentElement.scrollWidth > document.documentElement.clientWidth + 1,
  );
  expect(overflow, `${route} must not create document-level horizontal overflow`).toBe(false);
}

test('all customer routes remain responsive at desktop and mobile widths', async ({ page }) => {
  test.setTimeout(240_000);
  await enterCustomerDemo(page);
  for (const viewport of [
    { width: 1440, height: 900 },
    { width: 390, height: 844 },
  ]) {
    await page.setViewportSize(viewport);
    for (const route of customerRoutes) await assertRouteSurface(page, route);
  }
});

test('all operations routes remain responsive at desktop and mobile widths', async ({ page }) => {
  test.setTimeout(120_000);
  await page.goto('/login');
  await page.getByRole('button', { name: 'Explore the synthetic operations demo' }).click();
  for (const viewport of [
    { width: 1440, height: 900 },
    { width: 390, height: 844 },
  ]) {
    await page.setViewportSize(viewport);
    for (const route of operationsRoutes) await assertRouteSurface(page, route);
  }
});

test('critical controls expose correct pointer and disabled cursors', async ({ page }) => {
  await enterCustomerDemo(page);
  const themeButton = page.getByRole('button', { name: 'Switch to light mode' });
  expect(await themeButton.evaluate((element) => getComputedStyle(element).cursor)).toBe('pointer');
  await navigateSpa(page, '/account-services');
  const disabledSubmit = page.getByRole('button', { name: 'Submit request' });
  await expect(disabledSubmit).toBeDisabled();
  expect(await disabledSubmit.evaluate((element) => getComputedStyle(element).cursor)).toBe(
    'not-allowed',
  );
  expect(
    await page.getByLabel('Category').evaluate((element) => getComputedStyle(element).cursor),
  ).toBe('pointer');
});

test('dashboard has no horizontal overflow at every supported audit width', async ({ page }) => {
  await enterCustomerDemo(page);
  for (const width of [320, 390, 768, 1024, 1440, 1920]) {
    await page.setViewportSize({ width, height: width < 700 ? 844 : 900 });
    await navigateSpa(page, '/dashboard');
    const hasDocumentOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth > document.documentElement.clientWidth + 1,
    );
    expect(hasDocumentOverflow, `dashboard overflow at ${width}px`).toBe(false);
  }
});
