package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@SpringBootApplication
@RestController
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "job-service");
        response.put("status", "running");
        response.put("message", "Job service is up!");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "job-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "job-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs")
    public ResponseEntity<Map<String, Object>> createJob(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", UUID.randomUUID().toString());
        response.put("status", "QUEUED");
        response.put("type", request.getOrDefault("type", "DEFAULT"));
        response.put("message", "Job queued successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> getJobs() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> jobs = new ArrayList<>();

        String[] statuses = {"RUNNING", "COMPLETED", "QUEUED"};
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> job = new HashMap<>();
            job.put("id", UUID.randomUUID().toString());
            job.put("name", "job-" + i);
            job.put("status", statuses[i - 1]);
            jobs.add(job);
        }

        response.put("jobs", jobs);
        response.put("pod_name", System.getenv().getOrDefault("POD_NAME", "unknown"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "job-service");
        response.put("version", "1.0.0");
        response.put("environment", System.getenv().getOrDefault("APP_ENV", "local"));
        response.put("pod_name", System.getenv().getOrDefault("POD_NAME", "unknown"));
        response.put("node_name", System.getenv().getOrDefault("NODE_NAME", "unknown"));
        return ResponseEntity.ok(response);
    }
}
