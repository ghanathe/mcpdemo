package com.example.mcpdemo.repository;

import com.example.mcpdemo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerMcpCustomerId(String mcpCustomerId);
    Account findByMcpAccountId(String mcpAccountId);
}
