import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: true,
  retries: 1,
  reporter: 'line',
  use: { baseURL: 'http://127.0.0.1:4200', trace: 'retain-on-failure' },
  webServer: { command: 'npm.cmd start -- --host 127.0.0.1', url: 'http://127.0.0.1:4200', reuseExistingServer: true, timeout: 120_000 },
  projects: [{ name: 'chrome', use: { ...devices['Desktop Chrome'], channel: 'chrome' } }],
});
