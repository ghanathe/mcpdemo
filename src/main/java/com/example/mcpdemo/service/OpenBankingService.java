package com.example.mcpdemo.service;

import com.example.mcpdemo.config.OpenBankingConfig;
import com.example.mcpdemo.model.Account;
import com.example.mcpdemo.model.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenBankingService {
    
    private final OpenBankingConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private String accessToken;
    private long tokenExpiry;

    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiry) {
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken() {
        String url = config.getApiUrl() + "/aggregation/v2/partners/authentication";
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Finicity-App-Key", config.getAppKey());
        
        Map<String, String> body = new HashMap<>();
        body.put("partnerId", config.getPartnerId());
        body.put("partnerSecret", config.getPartnerSecret());
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            JsonNode.class
        );
        
        this.accessToken = response.getBody().get("token").asText();
        this.tokenExpiry = System.currentTimeMillis() + (90 * 60 * 1000); // 90 minutes
    }

    public Customer createTestCustomer(String username) {
        String url = config.getApiUrl() + "/aggregation/v2/customers/testing";
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Finicity-App-Key", config.getAppKey());
        headers.add("Finicity-App-Token", getAccessToken());
        
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            JsonNode.class
        );
        
        Customer customer = new Customer();
        customer.setMcpCustomerId(response.getBody().get("id").asText());
        customer.setUsername(username);
        customer.setCreatedDate(response.getBody().get("createdDate").asLong());
        
        return customer;
    }

    public String generateConnectUrl(String customerId, String redirectUri) {
        String url = config.getApiUrl() + "/connect/v2/generate";
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Finicity-App-Key", config.getAppKey());
        headers.add("Finicity-App-Token", getAccessToken());
        
        Map<String, Object> body = new HashMap<>();
        body.put("partnerId", config.getPartnerId());
        body.put("customerId", customerId);
        body.put("redirectUri", redirectUri);
        
        // Set singleUseUrl to true for security
        body.put("singleUseUrl", true);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            JsonNode.class
        );
        
        return response.getBody().get("link").asText();
    }

    public void refreshAccounts(String customerId) {
        String url = config.getApiUrl() + "/aggregation/v1/customers/" + customerId + "/accounts";
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Finicity-App-Key", config.getAppKey());
        headers.add("Finicity-App-Token", getAccessToken());
        
        HttpEntity<?> request = new HttpEntity<>(headers);
        
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            JsonNode.class
        );
    }

    public List<Account> getCustomerAccounts(String customerId) {
        String url = config.getApiUrl() + "/aggregation/v1/customers/" + customerId + "/accounts";
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Finicity-App-Key", config.getAppKey());
        headers.add("Finicity-App-Token", getAccessToken());
        
        HttpEntity<?> request = new HttpEntity<>(headers);
        
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            JsonNode.class
        );
        
        List<Account> accounts = new ArrayList<>();
        JsonNode accountsNode = response.getBody().get("accounts");
        
        if (accountsNode != null && accountsNode.isArray()) {
            for (JsonNode accountNode : accountsNode) {
                Account account = new Account();
                account.setMcpAccountId(accountNode.get("id").asText());
                account.setNumber(accountNode.get("number").asText());
                account.setName(accountNode.get("name").asText());
                account.setBalance(accountNode.get("balance").asDouble());
                account.setType(accountNode.get("type").asText());
                account.setStatus(accountNode.get("status").asText());
                account.setCurrency(accountNode.get("currency").asText());
                account.setBalanceDate(accountNode.get("balanceDate").asLong());
                accounts.add(account);
            }
        }
        
        return accounts;
    }
}
