import { expect, test, type Locator, type Page } from '@playwright/test';

test.describe.configure({ mode: 'serial' });
test.setTimeout(60_000);

const viewports = [
  { name: 'mobile', width: 390, height: 844 },
  { name: 'tablet', width: 768, height: 1024 },
  { name: 'desktop', width: 1440, height: 900 },
] as const;

async function openCustomerDemo(page: Page): Promise<void> {
  await page.goto('/login');
  await page.getByRole('button', { name: 'Explore the fictional customer demo' }).click();
  await expect(page).toHaveURL(/dashboard/);
}

async function openPreferences(page: Page): Promise<void> {
  const preferencesLink = page.getByRole('link', { name: 'Preferences' });
  if (!(await preferencesLink.isVisible())) {
    await page.getByRole('button', { name: 'Open navigation' }).click();
  }
  await preferencesLink.click();
  await expect(page).toHaveURL(/preferences/);
}

async function expectColorToken(locator: Locator, property: 'backgroundColor' | 'color', token: string): Promise<void> {
  await expect
    .poll(() =>
      locator.evaluate((element, [cssProperty, cssToken]) => {
        const styles = getComputedStyle(element);
        const probe = document.createElement('span');
        probe.style.color = `var(${cssToken})`;
        document.body.append(probe);
        const tokenColor = getComputedStyle(probe).color;
        probe.remove();
        return styles[cssProperty] === tokenColor;
      }, [property, token] as const),
    )
    .toBe(true);
}

async function expectNoLightSurfaceLeak(page: Page): Promise<void> {
  const leaks = await page.locator('body *:visible').evaluateAll((elements) =>
    elements
      .filter((element) => getComputedStyle(element).backgroundColor === 'rgb(255, 255, 255)')
      .map((element) => `${element.tagName.toLowerCase()}.${element.className}`)
      .slice(0, 10),
  );
  expect(leaks).toEqual([]);
}

for (const viewport of viewports) {
  test(`semantic surfaces remain consistent at ${viewport.name} width`, async ({ page }) => {
    await page.setViewportSize({ width: viewport.width, height: viewport.height });
    await openCustomerDemo(page);

    await expectColorToken(page.locator('.quick-actions'), 'backgroundColor', '--novara-surface');
    await expectColorToken(page.locator('.quick-actions a').first(), 'color', '--novara-text-secondary');

    await openPreferences(page);
    const themeSelect = page.getByLabel('Theme');
    await expect(themeSelect).toBeVisible();
    await expectColorToken(themeSelect, 'backgroundColor', '--novara-control-bg');
    await themeSelect.focus();
    await expect(themeSelect).toBeFocused();
    expect(await themeSelect.evaluate((element) => getComputedStyle(element).boxShadow)).not.toBe('none');

    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
    expect(overflow).toBe(false);
  });
}

test('light and dark themes resolve buttons and controls through semantic tokens', async ({ page }) => {
  await openCustomerDemo(page);
  await openPreferences(page);

  const select = page.getByLabel('Theme');
  const primary = page.getByRole('button', { name: 'Save application preferences' });
  await expectColorToken(select, 'backgroundColor', '--novara-control-bg');
  await expectColorToken(primary, 'backgroundColor', '--novara-action-primary');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await expect(select).toHaveValue('DARK');
  await expect(select.locator('option')).toHaveCount(2);
  await expect(select.locator('option')).toHaveText(['Dark', 'Light']);
  expect(await page.locator('html').evaluate((element) => getComputedStyle(element).colorScheme)).toBe('dark');

  await page.getByRole('button', { name: 'Switch to light mode' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await expectColorToken(select, 'backgroundColor', '--novara-control-bg');
  await expectColorToken(primary, 'backgroundColor', '--novara-action-primary');
  expect(await page.locator('html').evaluate((element) => getComputedStyle(element).colorScheme)).toBe('light');

  await page.getByRole('button', { name: 'Switch to dark mode' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  expect(await page.locator('html').evaluate((element) => getComputedStyle(element).colorScheme)).toBe('dark');
});

test('every primary customer page remains theme-safe in dark mode', async ({ page }) => {
  test.setTimeout(90_000);
  await openCustomerDemo(page);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');

  const destinations = [
    ['Dashboard', '/dashboard'],
    ['Accounts', '/accounts'],
    ['Payments', '/payments'],
    ['Cards', '/cards'],
    ['Deposits', '/deposits'],
    ['Loans', '/loans'],
    ['Investments', '/investments'],
    ['Statements', '/statements'],
    ['Account services', '/account-services'],
    ['Notifications', '/notifications'],
    ['Preferences', '/preferences'],
    ['Support', '/support'],
  ] as const;

  for (const [destination, path] of destinations) {
    await page.getByRole('complementary', { name: 'Primary navigation' }).getByRole('link', { name: destination, exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`${path}$`));
    await expectNoLightSurfaceLeak(page);
  }
});

test('login and every operations page remain theme-safe in dark mode', async ({ page }) => {
  test.setTimeout(90_000);
  await page.goto('/login');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await expectNoLightSurfaceLeak(page);
  await page.getByRole('button', { name: 'Explore the synthetic operations demo' }).click();

  const destinations = [
    ['Dashboard', '/operations-demo/dashboard'],
    ['Service requests', '/operations-demo/requests'],
    ['Customer directory', '/operations-demo/customers'],
    ['Account oversight', '/operations-demo/accounts'],
    ['Audit explorer', '/operations-demo/audit'],
    ['Outbox monitoring', '/operations-demo/monitoring'],
    ['Operations settings', '/operations-demo/settings'],
  ] as const;

  for (const [destination, path] of destinations) {
    await page.getByRole('link', { name: destination, exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`${path}$`));
    await expectNoLightSurfaceLeak(page);
  }
});

test('self-service hero cards retain the navy brand surface in dark mode', async ({ page }) => {
  await openCustomerDemo(page);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  const navigation = page.getByRole('complementary', { name: 'Primary navigation' });

  for (const destination of ['Account services', 'Notifications', 'Preferences'] as const) {
    await navigation.getByRole('link', { name: destination, exact: true }).click();
    const hero = page.locator('.hero');
    await expect(hero).toBeVisible();
    const colors = await hero.evaluate((element) => {
      const tokenProbe = document.createElement('span');
      tokenProbe.style.color = 'var(--novara-brand-panel)';
      document.body.append(tokenProbe);
      const brandPanel = getComputedStyle(tokenProbe).color;
      tokenProbe.remove();
      return { background: getComputedStyle(element).backgroundImage, brandPanel };
    });
    expect(colors.background).toContain(colors.brandPanel);
    expect(colors.background).not.toContain('rgb(213, 232, 244)');
  }
});
