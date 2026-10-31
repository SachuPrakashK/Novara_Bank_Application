import { expect, test } from '@playwright/test';

const viewports = [
  { name: 'desktop', width: 1440, height: 900, artworkVisible: true },
  { name: 'tablet', width: 1024, height: 768, artworkVisible: true },
  { name: 'mobile', width: 390, height: 844, artworkVisible: false },
] as const;

for (const viewport of viewports) {
  test(`login branding remains balanced at ${viewport.name} width`, async ({ page }) => {
    await page.setViewportSize({ width: viewport.width, height: viewport.height });
    await page.goto('/login');

    const story = page.locator('.login-story');
    await expect(story).toBeVisible();
    const brand = story.locator('img[alt="Novara Bank"]:visible');
    await expect(brand).toBeVisible();
    await expect(brand).toHaveAttribute('src', '/novara-logo-dark.png');
    await expect(page.getByRole('heading', { name: 'Sign in to Novara' })).toBeVisible();

    const artwork = page.locator('.banking-art');
    if (viewport.artworkVisible) {
      await expect(page.getByRole('heading', { name: 'Banking built around you.' })).toBeVisible();
      await expect(artwork).toBeVisible();
    } else {
      await expect(artwork).toBeHidden();
    }

    const overflow = await page.evaluate(() => {
      const viewportWidth = document.documentElement.clientWidth;
      return [...document.querySelectorAll<HTMLElement>('body *')]
        .filter((element) => element.getBoundingClientRect().right > viewportWidth + 1)
        .map((element) => ({ className: element.className, right: Math.round(element.getBoundingClientRect().right) }));
    });
    expect(overflow).toEqual([]);
  });
}
