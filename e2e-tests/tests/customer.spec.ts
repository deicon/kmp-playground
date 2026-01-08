import { test, expect, ApiHelpers } from './fixtures';

/**
 * Customer CRUD E2E Tests
 *
 * Tests the Customer management screen functionality:
 * - Viewing customer list
 * - Creating new customers
 * - Deleting customers
 * - Navigation to projects
 */

test.describe('Customer Management', () => {
  let apiHelpers: ApiHelpers;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async ({ page, testHelpers }) => {
    // Clean up test data before each test
    await apiHelpers.cleanupAllData();

    // Navigate to the app and wait for it to load
    await page.goto('/');
    await testHelpers.waitForAppLoad();
  });

  test.afterEach(async () => {
    // Clean up test data after each test
    await apiHelpers.cleanupAllData();
  });

  test.describe('Customer List', () => {
    test('should display empty state when no customers exist', async ({ page }) => {
      // Verify we're on the customers screen
      await expect(page.getByText('KMP Playground - Todo App')).toBeVisible();
      await expect(page.getByText('Customers')).toBeVisible();

      // Should not have any customer cards visible (only the header and add button)
      const addButton = page.getByRole('button', { name: /add customer/i });
      await expect(addButton).toBeVisible();
    });

    test('should display existing customers', async ({ page }) => {
      // Create customers via API
      await apiHelpers.createCustomer('Acme Corp', 'contact@acme.com');
      await apiHelpers.createCustomer('Beta Inc', 'info@beta.com');

      // Refresh to see the customers
      await page.reload();
      await page.waitForTimeout(2000); // Wait for WASM to reload

      // Verify customers are displayed
      await expect(page.getByText('Acme Corp')).toBeVisible();
      await expect(page.getByText('contact@acme.com')).toBeVisible();
      await expect(page.getByText('Beta Inc')).toBeVisible();
      await expect(page.getByText('info@beta.com')).toBeVisible();
    });

    test('should display customer without email correctly', async ({ page }) => {
      // Create customer without email
      await apiHelpers.createCustomer('No Email Customer');

      await page.reload();
      await page.waitForTimeout(2000);

      await expect(page.getByText('No Email Customer')).toBeVisible();
    });
  });

  test.describe('Create Customer', () => {
    test('should open add customer dialog', async ({ page }) => {
      // Click add customer button
      await page.getByRole('button', { name: /add customer/i }).click();

      // Verify dialog is open
      await expect(page.getByText('Add Customer')).toBeVisible();
      await expect(page.getByLabel(/name/i)).toBeVisible();
      await expect(page.getByLabel(/email/i)).toBeVisible();
      await expect(page.getByRole('button', { name: /cancel/i })).toBeVisible();
      await expect(page.getByRole('button', { name: /^add$/i })).toBeVisible();
    });

    test('should create customer with name only', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add customer/i }).click();

      // Fill in name
      await page.getByLabel(/name/i).fill('Test Customer');

      // Click add
      await page.getByRole('button', { name: /^add$/i }).click();

      // Wait for dialog to close and customer to appear
      await page.waitForTimeout(1000);

      // Verify customer was created
      await expect(page.getByText('Test Customer')).toBeVisible();

      // Verify via API
      const customers = await apiHelpers.getCustomers();
      expect(customers.length).toBe(1);
      expect(customers[0].name).toBe('Test Customer');
      expect(customers[0].email).toBeUndefined();
    });

    test('should create customer with name and email', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add customer/i }).click();

      // Fill in name and email
      await page.getByLabel(/name/i).fill('Full Customer');
      await page.getByLabel(/email/i).fill('full@customer.com');

      // Click add
      await page.getByRole('button', { name: /^add$/i }).click();

      await page.waitForTimeout(1000);

      // Verify customer was created
      await expect(page.getByText('Full Customer')).toBeVisible();
      await expect(page.getByText('full@customer.com')).toBeVisible();

      // Verify via API
      const customers = await apiHelpers.getCustomers();
      expect(customers.length).toBe(1);
      expect(customers[0].name).toBe('Full Customer');
      expect(customers[0].email).toBe('full@customer.com');
    });

    test('should cancel customer creation', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add customer/i }).click();

      // Fill in some data
      await page.getByLabel(/name/i).fill('Should Not Exist');

      // Click cancel
      await page.getByRole('button', { name: /cancel/i }).click();

      await page.waitForTimeout(500);

      // Verify customer was not created
      const customers = await apiHelpers.getCustomers();
      expect(customers.length).toBe(0);
    });

    test('should require name to submit', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add customer/i }).click();

      // Try to add without name - button should be disabled or not work
      const addButton = page.getByRole('button', { name: /^add$/i });

      // Try clicking add with empty name
      await addButton.click();

      // Dialog should still be open and no customer created
      await expect(page.getByText('Add Customer')).toBeVisible();
      const customers = await apiHelpers.getCustomers();
      expect(customers.length).toBe(0);
    });
  });

  test.describe('Delete Customer', () => {
    test('should delete customer', async ({ page }) => {
      // Create a customer
      await apiHelpers.createCustomer('To Delete', 'delete@me.com');

      await page.reload();
      await page.waitForTimeout(2000);

      // Verify customer exists
      await expect(page.getByText('To Delete')).toBeVisible();

      // Click delete button
      await page.getByRole('button', { name: /delete/i }).click();

      await page.waitForTimeout(1000);

      // Verify customer is gone from UI
      await expect(page.getByText('To Delete')).not.toBeVisible();

      // Verify via API
      const customers = await apiHelpers.getCustomers();
      expect(customers.length).toBe(0);
    });

    test('should delete correct customer when multiple exist', async ({ page }) => {
      // Create multiple customers
      await apiHelpers.createCustomer('Customer A');
      await apiHelpers.createCustomer('Customer B');
      await apiHelpers.createCustomer('Customer C');

      await page.reload();
      await page.waitForTimeout(2000);

      // Verify all customers exist
      await expect(page.getByText('Customer A')).toBeVisible();
      await expect(page.getByText('Customer B')).toBeVisible();
      await expect(page.getByText('Customer C')).toBeVisible();

      // Find the delete button for Customer B and click it
      // Since each customer card has its own delete button, we need to find the right one
      const customerBCard = page.getByText('Customer B').locator('..');
      const deleteButton = customerBCard.getByRole('button', { name: /delete/i });

      // If the above doesn't work due to Compose structure, try clicking all delete buttons
      // until Customer B is gone
      const deleteButtons = page.getByRole('button', { name: /delete/i });
      const count = await deleteButtons.count();

      // Click the second delete button (assuming order matches creation order)
      if (count >= 2) {
        await deleteButtons.nth(1).click();
      }

      await page.waitForTimeout(1000);

      // Verify Customer B is gone but others remain
      await expect(page.getByText('Customer A')).toBeVisible();
      await expect(page.getByText('Customer B')).not.toBeVisible();
      await expect(page.getByText('Customer C')).toBeVisible();

      // Verify via API
      const customers = await apiHelpers.getCustomers();
      expect(customers.length).toBe(2);
      expect(customers.map((c) => c.name)).not.toContain('Customer B');
    });
  });

  test.describe('Navigation', () => {
    test('should navigate to projects when clicking customer', async ({ page }) => {
      // Create a customer
      await apiHelpers.createCustomer('Navigate Test');

      await page.reload();
      await page.waitForTimeout(2000);

      // Click on the customer card (not the delete button)
      await page.getByText('Navigate Test').click();

      await page.waitForTimeout(1000);

      // Verify we're on the projects screen
      await expect(page.getByText('Projects')).toBeVisible();
      // Should see a back button
      await expect(page.getByRole('button', { name: /back/i })).toBeVisible();
    });

    test('should navigate back to customers from projects', async ({ page }) => {
      // Create a customer
      await apiHelpers.createCustomer('Back Test');

      await page.reload();
      await page.waitForTimeout(2000);

      // Navigate to projects
      await page.getByText('Back Test').click();
      await page.waitForTimeout(1000);

      // Verify we're on projects
      await expect(page.getByText('Projects')).toBeVisible();

      // Click back
      await page.getByRole('button', { name: /back/i }).click();

      await page.waitForTimeout(500);

      // Verify we're back on customers
      await expect(page.getByText('Customers')).toBeVisible();
      await expect(page.getByText('Back Test')).toBeVisible();
    });
  });
});
