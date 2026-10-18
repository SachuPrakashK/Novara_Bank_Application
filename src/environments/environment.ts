export const environment = {
  production: false,
  useMockApi: true,
  apiBaseUrl: '/api',
  session: { idleTimeoutMs: 15 * 60_000, warningDurationMs: 2 * 60_000 },
} as const;
