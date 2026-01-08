import { test, expect, ApiHelpers } from './fixtures';

/**
 * API E2E Tests
 *
 * These tests verify the backend API functionality directly.
 * Since Compose WASM renders to a canvas element, we focus on API testing
 * for reliable, maintainable tests.
 */

test.describe('Customer API', () => {
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

  test('should create a customer with name only', async () => {
    const customer = await apiHelpers.createCustomer('Test Customer');

    expect(customer.id).toBeDefined();
    expect(customer.name).toBe('Test Customer');
    expect(customer.email).toBeUndefined();
    expect(customer.createdAt).toBeDefined();
  });

  test('should create a customer with name and email', async () => {
    const customer = await apiHelpers.createCustomer('Full Customer', 'test@example.com');

    expect(customer.id).toBeDefined();
    expect(customer.name).toBe('Full Customer');
    expect(customer.email).toBe('test@example.com');
  });

  test('should get all customers', async () => {
    await apiHelpers.createCustomer('Customer A');
    await apiHelpers.createCustomer('Customer B');

    const customers = await apiHelpers.getCustomers();

    expect(customers.length).toBe(2);
    expect(customers.map((c) => c.name)).toContain('Customer A');
    expect(customers.map((c) => c.name)).toContain('Customer B');
  });

  test('should delete a customer', async () => {
    const customer = await apiHelpers.createCustomer('To Delete');
    await apiHelpers.deleteCustomer(customer.id);

    const customers = await apiHelpers.getCustomers();
    expect(customers.length).toBe(0);
  });
});

test.describe('Project API', () => {
  let apiHelpers: ApiHelpers;
  let testCustomerId: string;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async () => {
    await apiHelpers.cleanupAllData();
    const customer = await apiHelpers.createCustomer('Test Customer');
    testCustomerId = customer.id;
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test('should create a project with name only', async () => {
    const project = await apiHelpers.createProject(testCustomerId, 'Test Project');

    expect(project.id).toBeDefined();
    expect(project.customerId).toBe(testCustomerId);
    expect(project.name).toBe('Test Project');
    expect(project.description).toBeUndefined();
  });

  test('should create a project with description', async () => {
    const project = await apiHelpers.createProject(testCustomerId, 'Full Project', 'A detailed description');

    expect(project.name).toBe('Full Project');
    expect(project.description).toBe('A detailed description');
  });

  test('should get projects filtered by customer', async () => {
    const customer2 = await apiHelpers.createCustomer('Other Customer');

    await apiHelpers.createProject(testCustomerId, 'Project A');
    await apiHelpers.createProject(customer2.id, 'Project B');

    const projects = await apiHelpers.getProjects(testCustomerId);

    expect(projects.length).toBe(1);
    expect(projects[0].name).toBe('Project A');
  });

  test('should delete a project', async () => {
    const project = await apiHelpers.createProject(testCustomerId, 'To Delete');
    await apiHelpers.deleteProject(project.id);

    const projects = await apiHelpers.getProjects(testCustomerId);
    expect(projects.length).toBe(0);
  });
});

test.describe('Todo API', () => {
  let apiHelpers: ApiHelpers;
  let testProjectId: string;

  test.beforeAll(async () => {
    apiHelpers = new ApiHelpers();
  });

  test.beforeEach(async () => {
    await apiHelpers.cleanupAllData();
    const customer = await apiHelpers.createCustomer('Test Customer');
    const project = await apiHelpers.createProject(customer.id, 'Test Project');
    testProjectId = project.id;
  });

  test.afterEach(async () => {
    await apiHelpers.cleanupAllData();
  });

  test('should create a todo with title only', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'Test Todo');

    expect(todo.id).toBeDefined();
    expect(todo.projectId).toBe(testProjectId);
    expect(todo.title).toBe('Test Todo');
    expect(todo.completed).toBe(false);
    expect(todo.actualHours).toBe(0);
  });

  test('should create a todo with estimated hours', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'Estimated Todo', {
      estimatedHours: 4.5,
    });

    expect(todo.estimatedHours).toBe(4.5);
  });

  test('should create a todo with description', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'Detailed Todo', {
      description: 'This is a detailed description',
    });

    expect(todo.description).toBe('This is a detailed description');
  });

  test('should get todos filtered by project', async () => {
    const customer2 = await apiHelpers.createCustomer('Other Customer');
    const project2 = await apiHelpers.createProject(customer2.id, 'Other Project');

    await apiHelpers.createTodo(testProjectId, 'Todo A');
    await apiHelpers.createTodo(project2.id, 'Todo B');

    const todos = await apiHelpers.getTodos(testProjectId);

    expect(todos.length).toBe(1);
    expect(todos[0].title).toBe('Todo A');
  });

  test('should delete a todo', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'To Delete');
    await apiHelpers.deleteTodo(todo.id);

    const todos = await apiHelpers.getTodos(testProjectId);
    expect(todos.length).toBe(0);
  });

  test('should mark todo as completed', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'Complete Me');

    const response = await fetch(`http://localhost:8080/api/todos/${todo.id}/complete`, {
      method: 'POST',
    });
    expect(response.ok).toBe(true);

    const updated = await response.json();
    expect(updated.completed).toBe(true);
    expect(updated.completedAt).toBeDefined();
  });

  test('should mark todo as incomplete', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'Toggle Me');

    // First complete it
    await fetch(`http://localhost:8080/api/todos/${todo.id}/complete`, { method: 'POST' });

    // Then mark incomplete
    const response = await fetch(`http://localhost:8080/api/todos/${todo.id}/incomplete`, {
      method: 'POST',
    });
    expect(response.ok).toBe(true);

    const updated = await response.json();
    expect(updated.completed).toBe(false);
  });

  test('should update actual hours', async () => {
    const todo = await apiHelpers.createTodo(testProjectId, 'Track Hours');

    const response = await fetch(`http://localhost:8080/api/todos/${todo.id}/actual-hours`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ actualHours: 3.5 }),
    });
    expect(response.ok).toBe(true);

    const updated = await response.json();
    expect(updated.actualHours).toBe(3.5);
  });

  test('should get project time stats', async () => {
    await apiHelpers.createTodo(testProjectId, 'Todo 1', { estimatedHours: 2 });
    await apiHelpers.createTodo(testProjectId, 'Todo 2', { estimatedHours: 4 });
    const todo3 = await apiHelpers.createTodo(testProjectId, 'Todo 3', { estimatedHours: 2 });

    // Complete one todo
    await fetch(`http://localhost:8080/api/todos/${todo3.id}/complete`, { method: 'POST' });

    const response = await fetch(`http://localhost:8080/api/todos/projects/${testProjectId}/stats`);
    expect(response.ok).toBe(true);

    const stats = await response.json();
    expect(stats.projectId).toBe(testProjectId);
    expect(stats.totalEstimatedHours).toBe(8);
    expect(stats.todoCount).toBe(3);
    expect(stats.completedTodoCount).toBe(1);
  });
});

test.describe('Data Integrity', () => {
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

  test('should cascade delete projects when customer is deleted', async () => {
    const customer = await apiHelpers.createCustomer('To Delete');
    await apiHelpers.createProject(customer.id, 'Project 1');
    await apiHelpers.createProject(customer.id, 'Project 2');

    await apiHelpers.deleteCustomer(customer.id);

    const projects = await apiHelpers.getProjects(customer.id);
    expect(projects.length).toBe(0);
  });

  test('should cascade delete todos when project is deleted', async () => {
    const customer = await apiHelpers.createCustomer('Customer');
    const project = await apiHelpers.createProject(customer.id, 'To Delete');
    await apiHelpers.createTodo(project.id, 'Todo 1');
    await apiHelpers.createTodo(project.id, 'Todo 2');

    await apiHelpers.deleteProject(project.id);

    const todos = await apiHelpers.getTodos(project.id);
    expect(todos.length).toBe(0);
  });

  test('full hierarchy: customer -> projects -> todos', async () => {
    // Create customer
    const customer = await apiHelpers.createCustomer('Full Hierarchy', 'test@test.com');

    // Create projects
    const project1 = await apiHelpers.createProject(customer.id, 'Project 1', 'First project');
    const project2 = await apiHelpers.createProject(customer.id, 'Project 2', 'Second project');

    // Create todos
    await apiHelpers.createTodo(project1.id, 'P1 Todo 1', { estimatedHours: 2 });
    await apiHelpers.createTodo(project1.id, 'P1 Todo 2', { estimatedHours: 4 });
    await apiHelpers.createTodo(project2.id, 'P2 Todo 1', { estimatedHours: 8 });

    // Verify structure
    const customers = await apiHelpers.getCustomers();
    expect(customers.length).toBe(1);

    const projects = await apiHelpers.getProjects(customer.id);
    expect(projects.length).toBe(2);

    const todos1 = await apiHelpers.getTodos(project1.id);
    expect(todos1.length).toBe(2);

    const todos2 = await apiHelpers.getTodos(project2.id);
    expect(todos2.length).toBe(1);
  });
});
