package com.deicon.kmp_playground.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deicon.kmp_playground.models.Customer
import com.deicon.kmp_playground.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    onCustomerSelected: (String) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Customers",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Add Customer")
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(customers) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onCustomerSelected(customer.id) },
                        onDelete = {
                            scope.launch {
                                viewModel.deleteCustomer(customer.id)
                            }
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Customer") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCustomerName,
                            onValueChange = { newCustomerName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newCustomerEmail,
                            onValueChange = { newCustomerEmail = it },
                            label = { Text("Email (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.createCustomer(
                                    Customer(
                                        id = "",
                                        name = newCustomerName,
                                        email = newCustomerEmail.ifBlank { null },
                                        createdAt = Clock.System.now()
                                    )
                                )
                                newCustomerName = ""
                                newCustomerEmail = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium
                )
                customer.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
