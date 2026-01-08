import { FullConfig } from '@playwright/test';

const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3000';
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
const MAX_RETRIES = 60; // Maximum retries (60 * 2s = 2 minutes)
const RETRY_INTERVAL = 2000; // 2 seconds between retries

async function waitForService(url: string, name: string): Promise<void> {
  console.log(`Waiting for ${name} at ${url}...`);

  for (let i = 0; i < MAX_RETRIES; i++) {
    try {
      const response = await fetch(url, {
        method: 'GET',
        signal: AbortSignal.timeout(5000),
      });

      if (response.ok) {
        console.log(`${name} is ready!`);
        return;
      }
    } catch (error) {
      // Service not ready yet
    }

    if ((i + 1) % 10 === 0) {
      console.log(`Still waiting for ${name}... (${i + 1}/${MAX_RETRIES})`);
    }

    await new Promise((resolve) => setTimeout(resolve, RETRY_INTERVAL));
  }

  throw new Error(`${name} at ${url} did not become ready within timeout`);
}

async function globalSetup(config: FullConfig): Promise<void> {
  console.log('=== Global Setup: Waiting for services ===');

  // Wait for backend health endpoint
  await waitForService(`${BACKEND_URL}/health`, 'Backend');

  // Wait for frontend health endpoint
  await waitForService(`${FRONTEND_URL}/health`, 'Frontend');

  console.log('=== All services are ready! ===');
}

export default globalSetup;
