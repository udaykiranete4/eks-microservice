package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "gateway-service");
        response.put("status", "running");
        response.put("message", "Gateway is up! Routes: /payment, /jobs");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "gateway-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "gateway-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "gateway-service");
        response.put("version", "1.0.0");
        response.put("environment", System.getenv().getOrDefault("APP_ENV", "local"));
        response.put("pod_name", System.getenv().getOrDefault("POD_NAME", "unknown"));
        response.put("node_name", System.getenv().getOrDefault("NODE_NAME", "unknown"));
        return ResponseEntity.ok(response);
    }
}
