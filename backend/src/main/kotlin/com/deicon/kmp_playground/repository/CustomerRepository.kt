package com.deicon.kmp_playground.repository

import com.deicon.kmp_playground.models.Customer
import kotlinx.datetime.Instant
import javax.sql.DataSource

interface CustomerRepository {
    suspend fun getAll(): List<Customer>
    suspend fun getById(id: String): Customer?
    suspend fun create(customer: Customer): Customer
    suspend fun update(id: String, customer: Customer): Customer
    suspend fun delete(id: String): Boolean
}

class CustomerRepositoryImpl(private val dataSource: DataSource) : CustomerRepository {

    override suspend fun getAll(): List<Customer> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM customer ORDER BY created_at DESC").use { stmt ->
                val rs = stmt.executeQuery()
                val customers = mutableListOf<Customer>()
                while (rs.next()) {
                    customers.add(
                        Customer(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            email = rs.getString("email"),
                            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at"))
                        )
                    )
                }
                customers
            }
        }
    }

    override suspend fun getById(id: String): Customer? {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM customer WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    Customer(
                        id = rs.getString("id"),
                        name = rs.getString("name"),
                        email = rs.getString("email"),
                        createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at"))
                    )
                } else {
                    null
                }
            }
        }
    }

    override suspend fun create(customer: Customer): Customer {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO customer (id, name, email, created_at) VALUES (?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setString(1, customer.id)
                stmt.setString(2, customer.name)
                stmt.setString(3, customer.email)
                stmt.setLong(4, customer.createdAt.toEpochMilliseconds())
                stmt.executeUpdate()
            }
        }
        return customer
    }

    override suspend fun update(id: String, customer: Customer): Customer {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "UPDATE customer SET name = ?, email = ?, created_at = ? WHERE id = ?"
            ).use { stmt ->
                stmt.setString(1, customer.name)
                stmt.setString(2, customer.email)
                stmt.setLong(3, customer.createdAt.toEpochMilliseconds())
                stmt.setString(4, id)
                val updated = stmt.executeUpdate()
                if (updated == 0) {
                    throw NoSuchElementException("Customer not found: $id")
                }
            }
        }
        return customer.copy(id = id)
    }

    override suspend fun delete(id: String): Boolean {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM customer WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate() > 0
            }
        }
    }
}
