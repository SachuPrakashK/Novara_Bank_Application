import { expect, test } from '@playwright/test';

test('serves approved branding and favicon assets without missing requests', async ({
  page,
  request,
}) => {
  const missing: string[] = [];
  page.on('response', (response) => {
    if (response.status() >= 400 && new URL(response.url()).origin === 'http://127.0.0.1:4200') {
      missing.push(`${response.status()} ${response.url()}`);
    }
  });

  await page.goto('/login');
  const authLogo = page.locator('.login-brand img:visible');
  const mobileLogo = page.locator('.mobile-brand img');
  await expect(authLogo).toHaveAttribute('src', '/novara-logo-dark.png');
  await expect(authLogo).toHaveAttribute('alt', 'Novara Bank');
  await expect(mobileLogo).toHaveAttribute('src', '/novara-logo.png');
  await expect(mobileLogo).toHaveAttribute('alt', 'Novara Bank');
  expect(await authLogo.evaluate((image: HTMLImageElement) => image.naturalWidth)).toBe(1200);
  expect(await mobileLogo.evaluate((image: HTMLImageElement) => image.naturalWidth)).toBe(1200);

  const favicon = page.locator('link[rel="icon"][sizes="32x32"]');
  await expect(favicon).toHaveAttribute('href', '/favicon-32x32.png');
  for (const path of [
    '/novara-logo.png',
    '/novara-logo-dark.png',
    '/novara-icon.png',
    '/favicon.ico',
    '/favicon-32x32.png',
    '/favicon-16x16.png',
    '/apple-touch-icon.png',
  ]) {
    expect((await request.get(path)).ok(), `${path} should be served`).toBe(true);
  }
  expect(missing).toEqual([]);
});
test('switches from horizontal to compact branding with the sidebar layout', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  await page.goto('/login');
  await page.getByRole('button', { name: 'Explore the fictional customer demo' }).click();

  const sidebarLogo = page.locator('.sidebar novara-logo img');
  await expect(sidebarLogo).toBeVisible();
  await expect(sidebarLogo).toHaveAttribute('src', '/novara-logo-dark.png');
  expect((await sidebarLogo.boundingBox())?.width).toBeGreaterThan(100);
  await page.getByRole('button', { name: 'Switch to light mode' }).click();
  await expect(sidebarLogo).toHaveAttribute('src', '/novara-logo.png');
  await page.getByRole('button', { name: 'Collapse sidebar' }).click();
  await expect(sidebarLogo).toHaveAttribute('src', '/novara-icon.png');
  await expect(sidebarLogo).toHaveAttribute('alt', 'Novara Bank');

  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.locator('.mobile-header-brand img')).toHaveAttribute('src', '/novara-icon.png');
});

test('selects light and dark horizontal variants through the shared component', async ({
  page,
}) => {
  await page.goto('/login');
  await expect(page.locator('.login-brand img:visible')).toHaveAttribute(
    'src',
    '/novara-logo-dark.png',
  );
  await page.getByRole('button', { name: 'Explore the synthetic operations demo' }).click();
  await expect(page.locator('aside novara-logo img:visible')).toHaveAttribute(
    'src',
    '/novara-logo-dark.png',
  );
  await page.locator('html').evaluate((element) => {
    element.dataset['theme'] = 'light';
  });
  await expect(page.locator('aside novara-logo img:visible')).toHaveAttribute(
    'src',
    '/novara-logo.png',
  );
});
