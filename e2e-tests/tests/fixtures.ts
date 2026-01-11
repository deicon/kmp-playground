import { test as base, expect, Page, Locator } from '@playwright/test';

// API base URL for direct API calls (data cleanup, etc.)
const API_BASE_URL = process.env.BACKEND_URL || 'http://localhost:8080';

// Helper types
interface Customer {
  id: string;
  name: string;
  email?: string;
}

interface Project {
  id: string;
  customerId: string;
  name: string;
  description?: string;
}

interface Todo {
  id: string;
  projectId: string;
  title: string;
  description?: string;
  estimatedHours?: number;
  actualHours: number;
  completed: boolean;
}

// Test utilities
export class TestHelpers {
  constructor(private page: Page) {}

  /**
   * Wait for the WASM app to fully load
   */
  async waitForAppLoad(): Promise<void> {
    // Wait for loading message to disappear
    await this.page.waitForSelector('#loading', { state: 'hidden', timeout: 30000 });
    // Wait for canvas to be visible
    await this.page.waitForSelector('#ComposeTarget', { state: 'visible', timeout: 30000 });
    // Wait a bit for the Compose app to render
    await this.page.waitForTimeout(1000);
  }

  /**
   * Wait for network activity to settle
   */
  async waitForNetworkIdle(): Promise<void> {
    await this.page.waitForLoadState('networkidle', { timeout: 10000 });
  }

  /**
   * Take a snapshot for debugging
   */
  async snapshot(): Promise<string> {
    return await this.page.accessibility.snapshot() as unknown as string;
  }
}

// API helpers for test data management
export class ApiHelpers {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  /**
   * Create a customer via API
   */
  async createCustomer(name: string, email?: string): Promise<Customer> {
    const response = await fetch(`${this.baseUrl}/api/customers`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    if (!response.ok) {
      throw new Error(`Failed to create customer: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Get all customers via API
   */
  async getCustomers(): Promise<Customer[]> {
    const response = await fetch(`${this.baseUrl}/api/customers`);
    if (!response.ok) {
      throw new Error(`Failed to get customers: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Delete a customer via API
   */
  async deleteCustomer(id: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/customers/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok && response.status !== 404) {
      throw new Error(`Failed to delete customer: ${response.statusText}`);
    }
  }

  /**
   * Create a project via API
   */
  async createProject(customerId: string, name: string, description?: string): Promise<Project> {
    const response = await fetch(`${this.baseUrl}/api/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId, name, description }),
    });
    if (!response.ok) {
      throw new Error(`Failed to create project: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Get projects via API
   */
  async getProjects(customerId?: string): Promise<Project[]> {
    const url = customerId
      ? `${this.baseUrl}/api/projects?customerId=${customerId}`
      : `${this.baseUrl}/api/projects`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to get projects: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Delete a project via API
   */
  async deleteProject(id: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/projects/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok && response.status !== 404) {
      throw new Error(`Failed to delete project: ${response.statusText}`);
    }
  }

  /**
   * Create a todo via API
   */
  async createTodo(
    projectId: string,
    title: string,
    options?: { description?: string; estimatedHours?: number }
  ): Promise<Todo> {
    const response = await fetch(`${this.baseUrl}/api/todos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        projectId,
        title,
        description: options?.description,
        estimatedHours: options?.estimatedHours,
      }),
    });
    if (!response.ok) {
      throw new Error(`Failed to create todo: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Get todos via API
   */
  async getTodos(projectId?: string): Promise<Todo[]> {
    const url = projectId
      ? `${this.baseUrl}/api/todos?projectId=${projectId}`
      : `${this.baseUrl}/api/todos`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to get todos: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Delete a todo via API
   */
  async deleteTodo(id: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/api/todos/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok && response.status !== 404) {
      throw new Error(`Failed to delete todo: ${response.statusText}`);
    }
  }

  /**
   * Clean up all test data
   */
  async cleanupAllData(): Promise<void> {
    // Delete all customers (cascades to projects and todos)
    const customers = await this.getCustomers();
    for (const customer of customers) {
      await this.deleteCustomer(customer.id);
    }
  }
}

// Extended test fixture with helpers
export const test = base.extend<{
  testHelpers: TestHelpers;
  apiHelpers: ApiHelpers;
}>({
  testHelpers: async ({ page }, use) => {
    const helpers = new TestHelpers(page);
    await use(helpers);
  },
  apiHelpers: async ({}, use) => {
    const helpers = new ApiHelpers();
    await use(helpers);
  },
});

export { expect };
