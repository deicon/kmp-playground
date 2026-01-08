import { test, expect, ApiHelpers } from './fixtures';

/**
 * Todo CRUD E2E Tests with Time Tracking
 *
 * Tests the Todo management screen functionality:
 * - Viewing todo list (filtered by project)
 * - Creating new todos with time estimates
 * - Toggling todo completion
 * - Deleting todos
 * - Project time statistics
 * - Back navigation
 */

test.describe('Todo Management', () => {
  let apiHelpers: ApiHelpers;
  let testCustomerId: string;
  let testProjectId: string;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async ({ page, testHelpers }) => {
    // Clean up test data
    await apiHelpers.cleanupAllData();

    // Create test customer and project
    const customer = await apiHelpers.createCustomer('Test Customer for Todos');
    testCustomerId = customer.id;

    const project = await apiHelpers.createProject(testCustomerId, 'Test Project for Todos');
    testProjectId = project.id;

    // Navigate to the app
    await page.goto('/');
    await testHelpers.waitForAppLoad();

    // Navigate to the test project's todos
    await page.getByText('Test Customer for Todos').click();
    await page.waitForTimeout(1000);
    await page.getByText('Test Project for Todos').click();
    await page.waitForTimeout(1000);
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test.describe('Todo List', () => {
    test('should display todos screen with project context', async ({ page }) => {
      // Verify we're on the todos screen
      await expect(page.getByText('Todos')).toBeVisible();
      // Should show back button
      await expect(page.getByRole('button', { name: /back/i })).toBeVisible();
      // Should show add todo button
      await expect(page.getByRole('button', { name: /add todo/i })).toBeVisible();
    });

    test('should display project statistics card', async ({ page }) => {
      // The stats card should be visible even with no todos
      // It should show 0/0 todos and 0h estimated/actual
      await expect(page.getByText(/estimated/i)).toBeVisible();
      await expect(page.getByText(/actual/i)).toBeVisible();
      await expect(page.getByText(/0\/0/)).toBeVisible(); // 0/0 Todos
    });

    test('should display existing todos', async ({ page }) => {
      // Create todos via API
      await apiHelpers.createTodo(testProjectId, 'Todo One', { description: 'First todo' });
      await apiHelpers.createTodo(testProjectId, 'Todo Two', { estimatedHours: 2.5 });

      // Refresh and navigate back
      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Verify todos are displayed
      await expect(page.getByText('Todo One')).toBeVisible();
      await expect(page.getByText('First todo')).toBeVisible();
      await expect(page.getByText('Todo Two')).toBeVisible();
    });
  });

  test.describe('Create Todo', () => {
    test('should open add todo dialog', async ({ page }) => {
      // Click add todo button
      await page.getByRole('button', { name: /add todo/i }).click();

      // Verify dialog is open
      await expect(page.getByText('Add Todo')).toBeVisible();
      await expect(page.getByLabel(/title/i)).toBeVisible();
      await expect(page.getByLabel(/description/i)).toBeVisible();
      await expect(page.getByLabel(/estimated time/i)).toBeVisible();
      await expect(page.getByRole('button', { name: /cancel/i })).toBeVisible();
      await expect(page.getByRole('button', { name: /^add$/i })).toBeVisible();
    });

    test('should create todo with title only', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add todo/i }).click();

      // Fill in title
      await page.getByLabel(/title/i).fill('Simple Todo');

      // Click add
      await page.getByRole('button', { name: /^add$/i }).click();

      await page.waitForTimeout(1000);

      // Verify todo was created
      await expect(page.getByText('Simple Todo')).toBeVisible();

      // Verify via API
      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos.length).toBe(1);
      expect(todos[0].title).toBe('Simple Todo');
      expect(todos[0].completed).toBe(false);
    });

    test('should create todo with all fields', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add todo/i }).click();

      // Fill in all fields
      await page.getByLabel(/title/i).fill('Complete Todo');
      await page.getByLabel(/description/i).fill('This has all the details');
      await page.getByLabel(/estimated time/i).fill('4h');

      // Click add
      await page.getByRole('button', { name: /^add$/i }).click();

      await page.waitForTimeout(1000);

      // Verify todo was created
      await expect(page.getByText('Complete Todo')).toBeVisible();
      await expect(page.getByText('This has all the details')).toBeVisible();

      // Verify via API
      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos.length).toBe(1);
      expect(todos[0].title).toBe('Complete Todo');
      expect(todos[0].description).toBe('This has all the details');
      expect(todos[0].estimatedHours).toBe(4);
    });

    test('should cancel todo creation', async ({ page }) => {
      // Open dialog
      await page.getByRole('button', { name: /add todo/i }).click();

      // Fill in some data
      await page.getByLabel(/title/i).fill('Cancelled Todo');

      // Click cancel
      await page.getByRole('button', { name: /cancel/i }).click();

      await page.waitForTimeout(500);

      // Verify todo was not created
      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos.length).toBe(0);
    });
  });

  test.describe('Time Input Formats', () => {
    test('should accept hours format (2h)', async ({ page }) => {
      await page.getByRole('button', { name: /add todo/i }).click();
      await page.getByLabel(/title/i).fill('Hours Task');
      await page.getByLabel(/estimated time/i).fill('2h');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].estimatedHours).toBe(2);
    });

    test('should accept decimal hours format (2.5h)', async ({ page }) => {
      await page.getByRole('button', { name: /add todo/i }).click();
      await page.getByLabel(/title/i).fill('Decimal Hours Task');
      await page.getByLabel(/estimated time/i).fill('2.5h');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].estimatedHours).toBe(2.5);
    });

    test('should accept days format (1d = 8h)', async ({ page }) => {
      await page.getByRole('button', { name: /add todo/i }).click();
      await page.getByLabel(/title/i).fill('Day Task');
      await page.getByLabel(/estimated time/i).fill('1d');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].estimatedHours).toBe(8);
    });

    test('should accept decimal days format (1.5d = 12h)', async ({ page }) => {
      await page.getByRole('button', { name: /add todo/i }).click();
      await page.getByLabel(/title/i).fill('Half Day Task');
      await page.getByLabel(/estimated time/i).fill('1.5d');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].estimatedHours).toBe(12);
    });

    test('should accept weeks format (1w = 40h)', async ({ page }) => {
      await page.getByRole('button', { name: /add todo/i }).click();
      await page.getByLabel(/title/i).fill('Week Task');
      await page.getByLabel(/estimated time/i).fill('1w');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);

      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].estimatedHours).toBe(40);
    });
  });

  test.describe('Toggle Todo Completion', () => {
    test('should mark todo as completed', async ({ page }) => {
      // Create an incomplete todo
      await apiHelpers.createTodo(testProjectId, 'Toggle Test');

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Find and click the checkbox
      const checkbox = page.getByRole('checkbox').first();
      await expect(checkbox).not.toBeChecked();

      await checkbox.click();
      await page.waitForTimeout(1000);

      // Verify checkbox is now checked
      await expect(checkbox).toBeChecked();

      // Verify via API
      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].completed).toBe(true);
    });

    test('should mark todo as incomplete', async ({ page }) => {
      // Create a completed todo via API
      const todo = await apiHelpers.createTodo(testProjectId, 'Uncheck Test');
      // Mark it as completed via API
      await fetch(`http://localhost:8080/api/todos/${todo.id}/complete`, { method: 'POST' });

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Checkbox should be checked
      const checkbox = page.getByRole('checkbox').first();
      await expect(checkbox).toBeChecked();

      // Uncheck it
      await checkbox.click();
      await page.waitForTimeout(1000);

      // Verify checkbox is now unchecked
      await expect(checkbox).not.toBeChecked();

      // Verify via API
      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos[0].completed).toBe(false);
    });

    test('should update statistics when toggling completion', async ({ page }) => {
      // Create multiple todos
      await apiHelpers.createTodo(testProjectId, 'Task 1', { estimatedHours: 2 });
      await apiHelpers.createTodo(testProjectId, 'Task 2', { estimatedHours: 3 });

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Initially should show 0/2 completed
      await expect(page.getByText('0/2')).toBeVisible();

      // Complete one task
      const checkboxes = page.getByRole('checkbox');
      await checkboxes.first().click();
      await page.waitForTimeout(1000);

      // Should now show 1/2 completed
      await expect(page.getByText('1/2')).toBeVisible();
    });
  });

  test.describe('Delete Todo', () => {
    test('should delete todo', async ({ page }) => {
      // Create a todo
      await apiHelpers.createTodo(testProjectId, 'To Delete');

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Verify todo exists
      await expect(page.getByText('To Delete')).toBeVisible();

      // Click delete button
      await page.getByRole('button', { name: /delete/i }).click();

      await page.waitForTimeout(1000);

      // Verify todo is gone
      await expect(page.getByText('To Delete')).not.toBeVisible();

      // Verify via API
      const todos = await apiHelpers.getTodos(testProjectId);
      expect(todos.length).toBe(0);
    });
  });

  test.describe('Project Statistics', () => {
    test('should display total estimated hours', async ({ page }) => {
      // Create todos with estimated hours
      await apiHelpers.createTodo(testProjectId, 'Task 1', { estimatedHours: 2 });
      await apiHelpers.createTodo(testProjectId, 'Task 2', { estimatedHours: 4 });
      await apiHelpers.createTodo(testProjectId, 'Task 3', { estimatedHours: 2 });

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Total should be 8h (or 1d)
      // The UI formats this, so we check for the value
      await expect(page.getByText(/8h|1d/)).toBeVisible();
    });

    test('should display completion progress', async ({ page }) => {
      // Create todos
      await apiHelpers.createTodo(testProjectId, 'Complete 1');
      await apiHelpers.createTodo(testProjectId, 'Complete 2');
      const todo3 = await apiHelpers.createTodo(testProjectId, 'Complete 3');

      // Mark one as complete
      await fetch(`http://localhost:8080/api/todos/${todo3.id}/complete`, { method: 'POST' });

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Should show 1/3 completed
      await expect(page.getByText('1/3')).toBeVisible();
    });
  });

  test.describe('Navigation', () => {
    test('should navigate back to projects', async ({ page }) => {
      // Click back button
      await page.getByRole('button', { name: /back/i }).click();

      await page.waitForTimeout(500);

      // Verify we're back on projects
      await expect(page.getByText('Projects')).toBeVisible();
      await expect(page.getByText('Test Project for Todos')).toBeVisible();
    });

    test('should maintain context when navigating back and forth', async ({ page }) => {
      // Create a todo
      await apiHelpers.createTodo(testProjectId, 'Context Test Todo');

      await page.reload();
      await page.waitForTimeout(2000);
      await page.getByText('Test Customer for Todos').click();
      await page.waitForTimeout(1000);
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Verify todo is visible
      await expect(page.getByText('Context Test Todo')).toBeVisible();

      // Navigate back to projects
      await page.getByRole('button', { name: /back/i }).click();
      await page.waitForTimeout(500);

      // Navigate back to todos
      await page.getByText('Test Project for Todos').click();
      await page.waitForTimeout(1000);

      // Todo should still be visible
      await expect(page.getByText('Context Test Todo')).toBeVisible();
    });
  });
});
