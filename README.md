# Mastercard Open Banking Integration Documentation

This document describes the implementation of a Spring Boot application that integrates with Mastercard Open Banking APIs to verify account ownership and check balances.

## Project Overview

The application is a Spring Boot-based web application that allows users to:
1. Create a test customer
2. Connect their bank accounts using Mastercard Connect
3. View and refresh their connected accounts

## Technical Stack

- Spring Boot 3.5.4
- Spring Data JPA
- Thymeleaf for templating
- H2 Database for data storage
- Bootstrap 5.3.0 for UI

## Implementation Details

### Configuration

The application uses the following configuration in `application.properties`:

```properties
# Mastercard Open Banking API Configuration
mastercard.openbanking.api.url=https://api.finicity.com
mastercard.openbanking.partner.id=YOUR_PARTNER_ID
mastercard.openbanking.partner.secret=YOUR_PARTNER_SECRET
mastercard.openbanking.app.key=YOUR_APP_KEY
```

### Data Models

#### Customer Entity
```java
@Data
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mcpCustomerId;
    private String username;
    private Long createdDate;
}
```

#### Account Entity
```java
@Data
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String mcpAccountId;
    private String number;
    private String name;
    private Double balance;
    private String type;
    private String status;
    private Long balanceDate;
    private String currency;
    
    @ManyToOne
    private Customer customer;
}
```

### Key Components

1. OpenBankingConfig
   - Manages API configuration and provides a RestTemplate

2. OpenBankingService
   - Handles all Mastercard Open Banking API interactions
   - Manages access tokens
   - Implements customer creation, account connection, and data retrieval

3. OpenBankingController
   - Provides web endpoints for the application
   - Manages user flow through the application

### Key Features

1. Customer Creation
   ```java
   @PostMapping("/customer")
   public String createCustomer(@RequestParam String username, Model model) {
       Customer customer = openBankingService.createTestCustomer(username);
       customer = customerRepository.save(customer);
       
       String redirectUri = ServletUriComponentsBuilder.fromCurrentContextPath()
           .path("/connect/redirect")
           .queryParam("customerId", customer.getMcpCustomerId())
           .build()
           .toUriString();
           
       String connectUrl = openBankingService.generateConnectUrl(customer.getMcpCustomerId(), redirectUri);
       model.addAttribute("connectUrl", connectUrl);
       
       return "connect";
   }
   ```

2. Account Connection
   - Uses Mastercard Connect interface
   - Implements redirect flow for seamless user experience
   ```java
   @GetMapping("/connect/redirect")
   public String handleConnectRedirect(@RequestParam String customerId) {
       openBankingService.refreshAccounts(customerId);
       return "redirect:/accounts/" + customerId;
   }
   ```

3. Account Listing
   - Displays connected accounts in a table format
   - Shows account details including balance, type, and status
   - Provides refresh functionality

### User Flow

1. User enters username on the home page
2. System creates a test customer and generates a Connect URL
3. User connects their bank account through Mastercard Connect
4. System redirects to accounts page after successful connection
5. User can view and refresh their account information

### Security Considerations

1. Single-use Connect URLs
2. Secure redirect handling
3. Token management with expiration
4. Database security with proper entity relationships

### Future Improvements

1. Add proper authentication and authorization
2. Implement webhook handling for real-time updates
3. Add error handling and validation
4. Implement proper logging
5. Add transaction history viewing

## Testing

For testing purposes, use FinBank test profiles:
- Search for "FinBank Profiles -- A"
- Use username and password: `profile_03`

## Important Notes

1. Replace placeholder credentials in `application.properties` with actual Mastercard Open Banking credentials
2. Ensure proper error handling in production
3. Implement proper security measures before production deployment
4. Consider implementing webhook handling for real-time updates

## Resources

- [Mastercard Open Banking Documentation](https://developer.mastercard.com/open-banking-us/documentation/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
