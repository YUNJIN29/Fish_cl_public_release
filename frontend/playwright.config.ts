import { defineConfig, devices } from '@playwright/test'
export default defineConfig({
  testDir: './e2e', reporter: 'line',
  use: { baseURL: process.env.E2E_BASE_URL || 'http://127.0.0.1:4173', trace: 'retain-on-failure' },
  webServer: process.env.E2E_BASE_URL ? undefined : { command: 'npm run preview -- --host 127.0.0.1 --port 4173', port: 4173, reuseExistingServer: true },
  projects: [
    { name: 'mobile', use: { ...devices['iPhone 13'], browserName: 'chromium', channel: 'chrome', viewport: { width: 360, height: 800 } } },
    { name: 'tablet', use: { ...devices['iPad (gen 7)'], browserName: 'chromium', channel: 'chrome', viewport: { width: 768, height: 1024 } } },
    { name: 'desktop', use: { ...devices['Desktop Chrome'], channel: 'chrome', viewport: { width: 1280, height: 900 } } },
  ],
})
