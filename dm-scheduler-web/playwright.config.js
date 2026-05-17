const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './e2e',
  workers: 1,
  timeout: 120 * 1000,
  expect: {
    timeout: 10 * 1000
  },
  fullyParallel: false,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:8082',
    headless: true,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  webServer: [
    {
      command: 'mvn -q spring-boot:run',
      cwd: '../dm-scheduler',
      url: 'http://localhost:8081/health',
      reuseExistingServer: true,
      timeout: 300 * 1000
    },
    {
      command: 'npm run serve',
      url: 'http://localhost:8082',
      reuseExistingServer: true,
      timeout: 240 * 1000
    }
  ]
});
