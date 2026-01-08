import { test, expect, ApiHelpers } from './fixtures';

/**
 * Project CRUD E2E Tests
 *
 * Tests the Project management screen functionality:
 * - Viewing project list (filtered by customer)
 * - Creating new projects
 * - Deleting projects
 * - Navigation to todos
 * - Back navigation to customers
 */

test.describe('Project Management', () => {
  let apiHelpers: ApiHelpers;
  let testCustomerId: string;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async ({ page, testHelpers }) => {
    // Clean up test data
    await apiHelpers.cleanupAllData();

    // Create a test customer for project tests
    const customer = await apiHelpers.createCustomer('Test Customer for Projects', 'test@projects.com');
    testCustomerId = customer.id;

    // Navigate to the app
    await page.goto('/');
    await testHelpers.waitForAppLoad();

    // Navigate to the test customer's projects
    await page.getByText('Test Customer for Projects').click();
    await page.waitForTimeout(1000);
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test.describe('Project List', () => {
    test('should display projects screen with customer context', async ({ page }) => {
      // Verify we're on the projects screen
      await expect(page.getByText('Projects')).toBeVisible();
      // Should show back button
      await expect(page.getByRole('button', { name: /back/i })).toBeVisible();
      // Should show add project button
      await expect(page.getByRole('button', { name: /add project/i })).toBeVisible();
    });

    test('should display empty state when no projects exist', async ({ page }) => {
      // Verify projects screen is visible
      await expect(page.getByText('Projects')).toBeVisible();
      // Add button should be visible
      await expect(page.getByRole('button', { name: /add project/i })).toBeVisible();
    });

    test('should display existing projects', async ({ page }) => {
      // Create projects via API
      await apiHelpers.createProject(testCustomerId, 'Project Alpha', 'First project');
      await apiHelpers.createProject(testCustomerId, 'Project Beta', 'Second project');

      // Refresh to see the projects
      await page.reload();
      await page.waitForTimeout(2000);

      // Navigate back to projects
      await page.getByText('Test Customer for Projects').click();
      await page.waitForTimeout(1000);

      // Verify projects are displayed
      await expect(page.getByText('Project Alpha')).toBeVisible();
      await expect(page.getByText('First project')).toBeVisible();
      await expect(page.getByText('Project Beta')).toBeVisible();
      await expect(page.getByText('Second project')).toBeVisible();
    });

    test('should only show projects for selected customer', async ({ page }) => {
      // Create projects for test customer
      await apiHelpers.createProject(testCustomerId, 'My Project');

      // Create another customer with a project
      const otherCustomer = await apiHelpers.createCustomer('Other Customer');
      await apiHelpers.createProject(otherCustomer.id, 'Other Project');

      // Refresh
      await page.reload();
      await page.waitForTimeout(2000);

      // Navigate to test customer
      await page.getByText('Test Customer for Projects').click();
      await page.waitForTimeout(1000);

      // Should see My Project but not Other Project
      await expect(page.getByText('My Project')).toBeVisible();
      await expect(page.getByText('Other Project')).not.toBeVisible();

      // Go back and navigate to other customer
      await page.getByRole('button', { name: /back/i }).click();
      await page.waitForTimeout(500);
      await page.getByText('Other Customer').click();
      await page.waitForTimeout(1000);

      // Should see Other Project but not My Project
      await expect(page.getByText('Other Project')).toBeVisible();
      await expect(page.getByText('My Project')).not.toBeVisible();
    });
  });

  test.describe('Create Project', () => {
    test('should open add project dialog', async ({ page }) => {
      // Click add project button
      await page.getByRole('button', { name: /add project/i }).click();

      // Verify dialog is open
      await expect(page.getByText('Add Project')).toBeVisible();
      await expect(page.getByLabel(/name/i)).toBeVisible();
      await expect(page.getByLabel(/description/i)).toBeVisible();
      await expect(page.getByRole('button', { name: /cancel/i })).toBeVisible();
      await expect(page.getByRole('button', { name: /^add$/i })).toBeVisible();
    });

    test('should create project with name only', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add project/i }).click();

      // Fill in name
      await page.getByLabel(/name/i).fill('Simple Project');

      // Click add
      await page.getByRole('button', { name: /^add$/i }).click();

      await page.waitForTimeout(1000);

      // Verify project was created
      await expect(page.getByText('Simple Project')).toBeVisible();

      // Verify via API
      const projects = await apiHelpers.getProjects(testCustomerId);
      expect(projects.length).toBe(1);
      expect(projects[0].name).toBe('Simple Project');
      expect(projects[0].customerId).toBe(testCustomerId);
    });

    test('should create project with name and description', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add project/i }).click();

      // Fill in name and description
      await page.getByLabel(/name/i).fill('Detailed Project');
      await page.getByLabel(/description/i).fill('This is a detailed description');

      // Click add
      await page.getByRole('button', { name: /^add$/i }).click();

      await page.waitForTimeout(1000);

      // Verify project was created
      await expect(page.getByText('Detailed Project')).toBeVisible();
      await expect(page.getByText('This is a detailed description')).toBeVisible();

      // Verify via API
      const projects = await apiHelpers.getProjects(testCustomerId);
      expect(projects.length).toBe(1);
      expect(projects[0].name).toBe('Detailed Project');
      expect(projects[0].description).toBe('This is a detailed description');
    });

    test('should cancel project creation', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add project/i }).click();

      // Fill in some data
      await page.getByLabel(/name/i).fill('Cancelled Project');

      // Click cancel
      await page.getByRole('button', { name: /cancel/i }).click();

      await page.waitForTimeout(500);

      // Verify project was not created
      const projects = await apiHelpers.getProjects(testCustomerId);
      expect(projects.length).toBe(0);
    });

    test('should create multiple projects', async ({ page }) => {
      // Create first project
      await page.getByRole('button', { name: /add project/i }).click();
      await page.getByLabel(/name/i).fill('Project 1');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      // Create second project
      await page.getByRole('button', { name: /add project/i }).click();
      await page.getByLabel(/name/i).fill('Project 2');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      // Verify both projects are visible
      await expect(page.getByText('Project 1')).toBeVisible();
      await expect(page.getByText('Project 2')).toBeVisible();

      // Verify via API
      const projects = await apiHelpers.getProjects(testCustomerId);
      expect(projects.length).toBe(2);
    });
  });

  test.describe('Delete Project', () => {
    test('should delete project', async ({ page }) => {
      // Create a project
      await apiHelpers.createProject(testCustomerId, 'To Delete');

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Projects').click();
      await page.waitForTimeout(1000);

      // Verify project exists
      await expect(page.getByText('To Delete')).toBeVisible();

      // Click delete button
      await page.getByRole('button', { name: /delete/i }).click();

      await page.waitForTimeout(1000);

      // Verify project is gone
      await expect(page.getByText('To Delete')).not.toBeVisible();

      // Verify via API
      const projects = await apiHelpers.getProjects(testCustomerId);
      expect(projects.length).toBe(0);
    });

    test('should delete project with cascading todos', async ({ page }) => {
      // Create a project with todos
      const project = await apiHelpers.createProject(testCustomerId, 'Project With Todos');
      await apiHelpers.createTodo(project.id, 'Todo 1');
      await apiHelpers.createTodo(project.id, 'Todo 2');

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Projects').click();
      await page.waitForTimeout(1000);

      // Delete the project
      await page.getByRole('button', { name: /delete/i }).click();
      await page.waitForTimeout(1000);

      // Verify project and todos are gone
      const projects = await apiHelpers.getProjects(testCustomerId);
      expect(projects.length).toBe(0);

      const todos = await apiHelpers.getTodos(project.id);
      expect(todos.length).toBe(0);
    });
  });

  test.describe('Navigation', () => {
    test('should navigate to todos when clicking project', async ({ page }) => {
      // Create a project
      await apiHelpers.createProject(testCustomerId, 'Navigate to Todos');

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Projects').click();
      await page.waitForTimeout(1000);

      // Click on the project
      await page.getByText('Navigate to Todos').click();

      await page.waitForTimeout(1000);

      // Verify we're on the todos screen
      await expect(page.getByText('Todos')).toBeVisible();
      await expect(page.getByRole('button', { name: /back/i })).toBeVisible();
    });

    test('should navigate back to customers', async ({ page }) => {
      // Click back button
      await page.getByRole('button', { name: /back/i }).click();

      await page.waitForTimeout(500);

      // Verify we're back on customers
      await expect(page.getByText('Customers')).toBeVisible();
      await expect(page.getByText('Test Customer for Projects')).toBeVisible();
    });
  });
});
