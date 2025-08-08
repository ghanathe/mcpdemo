package com.example.mcpdemo.model;

import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

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
