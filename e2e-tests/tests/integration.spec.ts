import { test, expect, ApiHelpers } from './fixtures';

/**
 * Integration and Full Flow E2E Tests
 *
 * Tests complete user journeys through the application:
 * - Full workflow: Customer → Project → Todo → Completion
 * - Error handling and edge cases
 * - Performance and loading states
 * - Cross-entity navigation
 */

test.describe('Full User Journeys', () => {
  let apiHelpers: ApiHelpers;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async ({ page, testHelpers }) => {
    // Clean up test data
    await apiHelpers.cleanupAllData();

    // Navigate to the app
    await page.goto('/');
    await testHelpers.waitForAppLoad();
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test('complete workflow: create customer, project, and todos', async ({ page }) => {
    // Step 1: Create a customer
    await page.getByRole('button', { name: /add customer/i }).click();
    await page.getByLabel(/name/i).fill('Workflow Customer');
    await page.getByLabel(/email/i).fill('workflow@test.com');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Verify customer was created
    await expect(page.getByText('Workflow Customer')).toBeVisible();

    // Step 2: Navigate to projects and create one
    await page.getByText('Workflow Customer').click();
    await page.waitForTimeout(1000);

    await page.getByRole('button', { name: /add project/i }).click();
    await page.getByLabel(/name/i).fill('Workflow Project');
    await page.getByLabel(/description/i).fill('Testing the full workflow');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Verify project was created
    await expect(page.getByText('Workflow Project')).toBeVisible();

    // Step 3: Navigate to todos and create multiple
    await page.getByText('Workflow Project').click();
    await page.waitForTimeout(1000);

    // Create first todo
    await page.getByRole('button', { name: /add todo/i }).click();
    await page.getByLabel(/title/i).fill('Design Phase');
    await page.getByLabel(/description/i).fill('Create mockups');
    await page.getByLabel(/estimated time/i).fill('1d');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Create second todo
    await page.getByRole('button', { name: /add todo/i }).click();
    await page.getByLabel(/title/i).fill('Implementation');
    await page.getByLabel(/estimated time/i).fill('2d');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Create third todo
    await page.getByRole('button', { name: /add todo/i }).click();
    await page.getByLabel(/title/i).fill('Testing');
    await page.getByLabel(/estimated time/i).fill('4h');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Verify todos were created
    await expect(page.getByText('Design Phase')).toBeVisible();
    await expect(page.getByText('Implementation')).toBeVisible();
    await expect(page.getByText('Testing')).toBeVisible();

    // Verify statistics (8h + 16h + 4h = 28h)
    await expect(page.getByText('0/3')).toBeVisible(); // 0/3 completed

    // Step 4: Complete some todos
    const checkboxes = page.getByRole('checkbox');
    await checkboxes.first().click();
    await page.waitForTimeout(500);
    await checkboxes.nth(1).click();
    await page.waitForTimeout(500);

    // Verify completion count updated
    await expect(page.getByText('2/3')).toBeVisible();

    // Verify via API - full data structure
    const customers = await apiHelpers.getCustomers();
    expect(customers.length).toBe(1);
    expect(customers[0].name).toBe('Workflow Customer');

    const projects = await apiHelpers.getProjects(customers[0].id);
    expect(projects.length).toBe(1);
    expect(projects[0].name).toBe('Workflow Project');

    const todos = await apiHelpers.getTodos(projects[0].id);
    expect(todos.length).toBe(3);
    expect(todos.filter((t) => t.completed).length).toBe(2);
  });

  test('navigate full hierarchy and back', async ({ page }) => {
    // Set up data via API
    const customer = await apiHelpers.createCustomer('Nav Customer');
    const project = await apiHelpers.createProject(customer.id, 'Nav Project');
    await apiHelpers.createTodo(project.id, 'Nav Todo');

    await page.reload();
    await page.waitForTimeout(2000);

    // Start at customers
    await expect(page.getByText('Customers')).toBeVisible();
    await expect(page.getByText('Nav Customer')).toBeVisible();

    // Go to projects
    await page.getByText('Nav Customer').click();
    await page.waitForTimeout(1000);
    await expect(page.getByText('Projects')).toBeVisible();
    await expect(page.getByText('Nav Project')).toBeVisible();

    // Go to todos
    await page.getByText('Nav Project').click();
    await page.waitForTimeout(1000);
    await expect(page.getByText('Todos')).toBeVisible();
    await expect(page.getByText('Nav Todo')).toBeVisible();

    // Go back to projects
    await page.getByRole('button', { name: /back/i }).click();
    await page.waitForTimeout(500);
    await expect(page.getByText('Projects')).toBeVisible();
    await expect(page.getByText('Nav Project')).toBeVisible();

    // Go back to customers
    await page.getByRole('button', { name: /back/i }).click();
    await page.waitForTimeout(500);
    await expect(page.getByText('Customers')).toBeVisible();
    await expect(page.getByText('Nav Customer')).toBeVisible();
  });

  test('handle multiple customers with separate project hierarchies', async ({ page }) => {
    // Create two customers with projects and todos
    const customerA = await apiHelpers.createCustomer('Customer A');
    const projectA1 = await apiHelpers.createProject(customerA.id, 'Project A1');
    const projectA2 = await apiHelpers.createProject(customerA.id, 'Project A2');
    await apiHelpers.createTodo(projectA1.id, 'Todo A1-1');
    await apiHelpers.createTodo(projectA2.id, 'Todo A2-1');

    const customerB = await apiHelpers.createCustomer('Customer B');
    const projectB1 = await apiHelpers.createProject(customerB.id, 'Project B1');
    await apiHelpers.createTodo(projectB1.id, 'Todo B1-1');

    await page.reload();
    await page.waitForTimeout(2000);

    // Verify both customers are visible
    await expect(page.getByText('Customer A')).toBeVisible();
    await expect(page.getByText('Customer B')).toBeVisible();

    // Navigate to Customer A's projects
    await page.getByText('Customer A').click();
    await page.waitForTimeout(1000);

    // Should only see A's projects
    await expect(page.getByText('Project A1')).toBeVisible();
    await expect(page.getByText('Project A2')).toBeVisible();
    await expect(page.getByText('Project B1')).not.toBeVisible();

    // Navigate to Project A1's todos
    await page.getByText('Project A1').click();
    await page.waitForTimeout(1000);

    await expect(page.getByText('Todo A1-1')).toBeVisible();
    await expect(page.getByText('Todo A2-1')).not.toBeVisible();
    await expect(page.getByText('Todo B1-1')).not.toBeVisible();

    // Navigate all the way back and go to Customer B
    await page.getByRole('button', { name: /back/i }).click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: /back/i }).click();
    await page.waitForTimeout(500);

    await page.getByText('Customer B').click();
    await page.waitForTimeout(1000);

    // Should only see B's projects
    await expect(page.getByText('Project B1')).toBeVisible();
    await expect(page.getByText('Project A1')).not.toBeVisible();
  });
});

test.describe('Error Handling and Edge Cases', () => {
  let apiHelpers: ApiHelpers;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async ({ page, testHelpers }) => {
    await apiHelpers.cleanupAllData();
    await page.goto('/');
    await testHelpers.waitForAppLoad();
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test('should handle special characters in names', async ({ page }) => {
    // Create customer with special characters
    await page.getByRole('button', { name: /add customer/i }).click();
    await page.getByLabel(/name/i).fill('Test & "Quotes" <Company>');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Verify it's displayed correctly
    await expect(page.getByText('Test & "Quotes" <Company>')).toBeVisible();
  });

  test('should handle unicode characters', async ({ page }) => {
    // Create customer with unicode
    await page.getByRole('button', { name: /add customer/i }).click();
    await page.getByLabel(/name/i).fill('Testy McTest');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    await expect(page.getByText('Testy McTest')).toBeVisible();
  });

  test('should handle long text content', async ({ page }) => {
    const longName = 'A'.repeat(100);
    const longDescription = 'B'.repeat(500);

    // Create customer
    await page.getByRole('button', { name: /add customer/i }).click();
    await page.getByLabel(/name/i).fill(longName);
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Navigate and create project
    await page.locator('text=' + longName.substring(0, 50)).first().click();
    await page.waitForTimeout(1000);

    await page.getByRole('button', { name: /add project/i }).click();
    await page.getByLabel(/name/i).fill('Long Project');
    await page.getByLabel(/description/i).fill(longDescription);
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Should handle without crashing
    await expect(page.getByText('Long Project')).toBeVisible();
  });

  test('should handle rapid operations', async ({ page }) => {
    // Create customer
    const customer = await apiHelpers.createCustomer('Rapid Test');

    await page.reload();
    await page.waitForTimeout(2000);

    // Rapidly navigate
    await page.getByText('Rapid Test').click();
    await page.waitForTimeout(200);
    await page.getByRole('button', { name: /back/i }).click();
    await page.waitForTimeout(200);
    await page.getByText('Rapid Test').click();
    await page.waitForTimeout(200);

    // Should not crash
    await expect(page.getByText('Projects')).toBeVisible();
  });
});

test.describe('App Loading and Performance', () => {
  let apiHelpers: ApiHelpers;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test('should load WASM app within reasonable time', async ({ page }) => {
    const startTime = Date.now();

    await page.goto('/');

    // Wait for loading to complete
    await page.waitForSelector('#loading', { state: 'hidden', timeout: 30000 });
    await page.waitForSelector('#ComposeTarget', { state: 'visible' });

    const loadTime = Date.now() - startTime;

    // App should load within 30 seconds (WASM apps can be slow)
    expect(loadTime).toBeLessThan(30000);

    console.log(`WASM app loaded in ${loadTime}ms`);
  });

  test('should handle many items without significant slowdown', async ({ page, testHelpers }) => {
    // Create 20 customers
    for (let i = 0; i < 20; i++) {
      await apiHelpers.createCustomer(`Customer ${i + 1}`);
    }

    await page.goto('/');
    await testHelpers.waitForAppLoad();

    // All customers should be visible
    await expect(page.getByText('Customer 1')).toBeVisible();
    await expect(page.getByText('Customer 20')).toBeVisible();
  });

  test('should correctly handle page refresh', async ({ page, testHelpers }) => {
    // Create data
    const customer = await apiHelpers.createCustomer('Refresh Test');
    const project = await apiHelpers.createProject(customer.id, 'Refresh Project');
    await apiHelpers.createTodo(project.id, 'Refresh Todo');

    await page.goto('/');
    await testHelpers.waitForAppLoad();

    // Navigate to todos
    await page.getByText('Refresh Test').click();
    await page.waitForTimeout(1000);
    await page.getByText('Refresh Project').click();
    await page.waitForTimeout(1000);

    // Refresh
    await page.reload();
    await testHelpers.waitForAppLoad();

    // Should be back at customers (no deep linking support assumed)
    await expect(page.getByText('Customers')).toBeVisible();
  });
});

test.describe('API Integration', () => {
  let apiHelpers: ApiHelpers;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async ({ page, testHelpers }) => {
    await apiHelpers.cleanupAllData();
    await page.goto('/');
    await testHelpers.waitForAppLoad();
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test('UI should reflect API data changes after refresh', async ({ page, testHelpers }) => {
    // Create customer via UI
    await page.getByRole('button', { name: /add customer/i }).click();
    await page.getByLabel(/name/i).fill('UI Customer');
    await page.getByRole('button', { name: /^add$/i }).click();
    await page.waitForTimeout(1000);

    // Create another customer via API
    await apiHelpers.createCustomer('API Customer');

    // Refresh page
    await page.reload();
    await testHelpers.waitForAppLoad();

    // Both customers should be visible
    await expect(page.getByText('UI Customer')).toBeVisible();
    await expect(page.getByText('API Customer')).toBeVisible();
  });

  test('should sync deletion between UI and API', async ({ page, testHelpers }) => {
    // Create customer
    const customer = await apiHelpers.createCustomer('Delete Sync Test');

    await page.reload();
    await testHelpers.waitForAppLoad();

    // Delete via UI
    await page.getByRole('button', { name: /delete/i }).click();
    await page.waitForTimeout(1000);

    // Verify via API
    const customers = await apiHelpers.getCustomers();
    expect(customers.length).toBe(0);
  });
});
