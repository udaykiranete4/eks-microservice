package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@SpringBootApplication
@RestController
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "payment-service");
        response.put("status", "running");
        response.put("message", "Payment service is up!");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", UUID.randomUUID().toString());
        response.put("status", "SUCCESS");
        response.put("amount", request.getOrDefault("amount", 0));
        response.put("currency", request.getOrDefault("currency", "USD"));
        response.put("message", "Payment processed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> txns = new ArrayList<>();

        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", UUID.randomUUID().toString());
        t1.put("amount", 100.00);
        t1.put("status", "SUCCESS");
        txns.add(t1);

        response.put("transactions", txns);
        response.put("pod_name", System.getenv().getOrDefault("POD_NAME", "unknown"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "payment-service");
        response.put("version", "1.0.0");
        response.put("environment", System.getenv().getOrDefault("APP_ENV", "local"));
        response.put("pod_name", System.getenv().getOrDefault("POD_NAME", "unknown"));
        response.put("node_name", System.getenv().getOrDefault("NODE_NAME", "unknown"));
        return ResponseEntity.ok(response);
    }
}
