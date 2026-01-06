package com.deicon.kmp_playground.viewmodel

import com.deicon.kmp_playground.api.TodoApiClient
import com.deicon.kmp_playground.models.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomerViewModel(private val apiClient: TodoApiClient) {
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadCustomers() {
        _loading.value = true
        _error.value = null
        try {
            _customers.value = apiClient.getAllCustomers()
        } catch (e: Exception) {
            _error.value = "Failed to load customers: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun createCustomer(customer: Customer) {
        _loading.value = true
        _error.value = null
        try {
            apiClient.createCustomer(customer)
            loadCustomers()
        } catch (e: Exception) {
            _error.value = "Failed to create customer: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun deleteCustomer(id: String) {
        _loading.value = true
        _error.value = null
        try {
            apiClient.deleteCustomer(id)
            loadCustomers()
        } catch (e: Exception) {
            _error.value = "Failed to delete customer: ${e.message}"
        } finally {
            _loading.value = false
        }
    }
}
