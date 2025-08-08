package com.example.mcpdemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenBankingConfig {

    @Value("${mastercard.openbanking.api.url}")
    private String apiUrl;

    @Value("${mastercard.openbanking.partner.id}")
    private String partnerId;

    @Value("${mastercard.openbanking.partner.secret}")
    private String partnerSecret;

    @Value("${mastercard.openbanking.app.key}")
    private String appKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getPartnerSecret() {
        return partnerSecret;
    }

    public String getAppKey() {
        return appKey;
    }
}
