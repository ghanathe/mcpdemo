package com.example.mcpdemo.controller;

import com.example.mcpdemo.model.Account;
import com.example.mcpdemo.model.Customer;
import com.example.mcpdemo.repository.CustomerRepository;
import com.example.mcpdemo.service.OpenBankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OpenBankingController {

    private final OpenBankingService openBankingService;
    private final CustomerRepository customerRepository;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/customer")
    public String createCustomer(@RequestParam String username, Model model) {
        Customer customer = openBankingService.createTestCustomer(username);
        customer = customerRepository.save(customer);
        
        String redirectUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/connect/redirect")
            .queryParam("customerId", customer.getMcpCustomerId())
            .build()
            .toUriString();
            
        String connectUrl = openBankingService.generateConnectUrl(customer.getMcpCustomerId(), redirectUrl);
        model.addAttribute("connectUrl", connectUrl);
        
        return "connect";
    }
    
    @GetMapping("/connect/redirect")
    public String handleConnectRedirect(@RequestParam String customerId) {
        // Refresh the accounts immediately after connection
        openBankingService.refreshAccounts(customerId);
        return "redirect:/accounts/" + customerId;
    }

    @GetMapping("/refresh/{customerId}")
    public String refreshAccounts(@PathVariable String customerId) {
        openBankingService.refreshAccounts(customerId);
        return "redirect:/accounts/" + customerId;
    }

    @GetMapping("/accounts/{customerId}")
    public String getAccounts(@PathVariable String customerId, Model model) {
        Customer customer = customerRepository.findByMcpCustomerId(customerId);
        if (customer == null) {
            return "redirect:/";
        }

        List<Account> accounts = openBankingService.getCustomerAccounts(customerId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("customerId", customerId);
        
        return "accounts";
    }
}
