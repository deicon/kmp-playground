import { test, expect, ApiHelpers } from './fixtures';

/**
 * App Smoke Tests
 *
 * Basic tests to verify the Compose WASM app loads and renders correctly.
 * Since Compose renders to a canvas, we use visual verification rather than DOM queries.
 */

test.describe('App Loading', () => {
  test('should load the WASM app successfully', async ({ page, testHelpers }) => {
    await page.goto('/');

    // Wait for loading indicator to disappear
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 60000 });

    // Verify the canvas element exists and is visible
    const canvas = page.locator('#ComposeTarget');
    await expect(canvas).toBeVisible();

    // Verify the canvas has dimensions (app rendered)
    const box = await canvas.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.width).toBeGreaterThan(0);
    expect(box!.height).toBeGreaterThan(0);
  });

  test('should render without JavaScript errors', async ({ page }) => {
    const errors: string[] = [];

    page.on('pageerror', (error) => {
      errors.push(error.message);
    });

    await page.goto('/');
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 60000 });

    // Allow some time for any deferred errors
    await page.waitForTimeout(2000);

    // Filter out known WASM-related warnings
    const criticalErrors = errors.filter(
      (e) => !e.includes('wasm') && !e.includes('SharedArrayBuffer')
    );

    expect(criticalErrors).toHaveLength(0);
  });

  test('should make successful API calls on load', async ({ page }) => {
    const apiCalls: { url: string; status: number }[] = [];

    page.on('response', (response) => {
      if (response.url().includes('/api/')) {
        apiCalls.push({ url: response.url(), status: response.status() });
      }
    });

    await page.goto('/');
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 60000 });

    // Wait for initial data load
    await page.waitForTimeout(3000);

    // Verify API calls were made and succeeded
    const customerCalls = apiCalls.filter((c) => c.url.includes('/api/customers'));
    expect(customerCalls.length).toBeGreaterThan(0);
    expect(customerCalls.every((c) => c.status === 200)).toBe(true);
  });

  test('should take baseline screenshot for visual comparison', async ({ page, testHelpers }) => {
    const apiHelpers = new ApiHelpers();
    await apiHelpers.cleanupAllData();

    // Create some sample data
    const customer = await apiHelpers.createCustomer('Screenshot Customer', 'test@example.com');
    await apiHelpers.createProject(customer.id, 'Sample Project', 'For visual testing');

    await page.goto('/');
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 60000 });
    await page.waitForTimeout(2000);

    // Take screenshot
    await page.screenshot({ path: 'test-results/app-with-data.png', fullPage: true });

    // Cleanup
    await apiHelpers.cleanupAllData();
  });
});

test.describe('Network Health', () => {
  test('backend health endpoint should respond', async ({ page }) => {
    const response = await page.request.get('http://localhost:8080/health');
    expect(response.ok()).toBe(true);
  });

  test('frontend health endpoint should respond', async ({ page }) => {
    const response = await page.request.get('http://localhost:3000/health');
    expect(response.ok()).toBe(true);
  });

  test('API proxy should work correctly', async ({ page }) => {
    // The frontend proxies /api/* to the backend
    const response = await page.request.get('http://localhost:3000/api/customers');
    expect(response.ok()).toBe(true);
  });
});

test.describe('WASM Loading', () => {
  test('should load WASM files with correct MIME type', async ({ page }) => {
    let wasmLoaded = false;

    page.on('response', (response) => {
      if (response.url().endsWith('.wasm')) {
        const contentType = response.headers()['content-type'];
        expect(contentType).toContain('application/wasm');
        wasmLoaded = true;
      }
    });

    await page.goto('/');
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 60000 });

    expect(wasmLoaded).toBe(true);
  });

  test('should load JavaScript bundle', async ({ page }) => {
    let jsLoaded = false;

    page.on('response', (response) => {
      if (response.url().includes('frontend.js')) {
        expect(response.status()).toBe(200);
        jsLoaded = true;
      }
    });

    await page.goto('/');
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 60000 });

    expect(jsLoaded).toBe(true);
  });
});
