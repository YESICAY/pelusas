import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 30000,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  webServer: [
    {
      command: 'mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"',
      cwd: '../backend',
      url: 'http://localhost:8080/api/historial',
      timeout: 180000,
      reuseExistingServer: false,
    },
    {
      command: 'npm run dev',
      url: 'http://localhost:5173',
      timeout: 60000,
      reuseExistingServer: true,
    },
  ],
})