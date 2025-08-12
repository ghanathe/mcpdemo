package com.example.mcpdemo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.mastercard.developer.oauth.OAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.PrivateKey;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.net.URI;

@Service
public class CurrencyConversionService {

    @Value("${mastercard.currency.api.url}")
    private String apiUrl;

    @Value("${mastercard.currency.api.consumer.key}")
    private String consumerKey;

    @Value("${mastercard.currency.api.keystore.path}")
    private String keystorePath;

    @Value("${mastercard.currency.api.keystore.password}")
    private String keystorePassword;

    @Value("${mastercard.currency.api.keystore.alias}")
    private String keystoreAlias;

    private final RestTemplate restTemplate;

    public CurrencyConversionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public double convertToINR(double amount, String fromCurrency) {
        return convertAmount(amount, fromCurrency, "INR");
    }

    public static class ConversionResponse {
        @JsonProperty("data")
        private ConversionData data;

        public ConversionData getData() {
            return data;
        }

        public void setData(ConversionData data) {
            this.data = data;
        }

        public String toString() {
            return "ConversionResponse{" +
                    "data=" + data +
                    '}';
        }

         
        public static class ConversionData {
            private Double conversionRate;
            private Double crdhldBillAmt;
            private String fxDate;
            private String transCurr;
            private String crdhldBillCurr;
            private Double transAmt;
            public Double getConversionRate() {
                return conversionRate;
            }
            public void setConversionRate(Double conversionRate) {
                this.conversionRate = conversionRate;
            }
            public Double getCrdhldBillAmt() {
                return crdhldBillAmt;
            }
            public void setCrdhldBillAmt(Double crdhldBillAmt) {
                this.crdhldBillAmt = crdhldBillAmt;
            }
            public String getFxDate() {
                return fxDate;
            }
            public void setFxDate(String fxDate) {
                this.fxDate = fxDate;
            }
            public String getTransCurr() {
                return transCurr;
            }
            public void setTransCurr(String transCurr) {
                this.transCurr = transCurr;
            }
            public String getCrdhldBillCurr() {
                return crdhldBillCurr;
            }
            public void setCrdhldBillCurr(String crdhldBillCurr) {
                this.crdhldBillCurr = crdhldBillCurr;
            }
            public Double getTransAmt() {
                return transAmt;
            }
            public void setTransAmt(Double transAmt) {
                this.transAmt = transAmt;
            }

            public String toString() {
                return "ConversionData{" +
                        "conversionRate=" + conversionRate +
                        ", crdhldBillAmt=" + crdhldBillAmt +
                        ", fxDate='" + fxDate + '\'' +
                        ", transCurr='" + transCurr + '\'' +
                        ", crdhldBillCurr='" + crdhldBillCurr + '\'' +
                        ", transAmt=" + transAmt +
                        '}';
            }
        }
    }

    public double convertAmount(double amount, String fromCurrency, String toCurrency) {
        try {
            // Load the signing key
            PrivateKey signingKey = loadSigningKey();

            // Create the request URL for the new API endpoint
            String requestUrl = String.format("%s/conversion-rate?fxDate=2025-08-08&transCurr=%s&bankFee=0&crdhldBillCurr=%s&transAmt=%s",
                    apiUrl, fromCurrency, toCurrency, amount < 0 ? -amount : amount);

            System.out.println("Request URL: " + requestUrl);

            // Sign the request
            String authHeader = OAuth.getAuthorizationHeader(
                new URI(requestUrl),
                "GET",
                "",
                java.nio.charset.StandardCharsets.UTF_8,
                consumerKey,
                signingKey
            );
            System.out.println("Authorization Header: " + authHeader);

            // Add the authorization header to the request
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", authHeader);
            
            // Make the API call
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<ConversionResponse> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                ConversionResponse.class
            );

           
            
            return response != null && response.getBody() != null && response.getBody().getData() != null ? 
                amount < 0 ? -response.getBody().getData().getCrdhldBillAmt():response.getBody().getData().getCrdhldBillAmt() : amount;
        } catch (Exception e) {
            // Log the error and return original amount if conversion fails
            e.printStackTrace();
            return amount;
        }
    }

    private PrivateKey loadSigningKey() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        FileInputStream is = new FileInputStream(keystorePath);
        keystore.load(is, keystorePassword.toCharArray());
        return (PrivateKey) keystore.getKey(keystoreAlias, keystorePassword.toCharArray());
    }
}
