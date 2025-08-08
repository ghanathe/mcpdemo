package com.example.mcpdemo.repository;

import com.example.mcpdemo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByMcpCustomerId(String mcpCustomerId);
}
