export const environment = {
  production: false,
  useMockApi: false,
  apiBaseUrl: '/api/v1',
  session: { idleTimeoutMs: 15 * 60_000, warningDurationMs: 2 * 60_000 },
} as const;
