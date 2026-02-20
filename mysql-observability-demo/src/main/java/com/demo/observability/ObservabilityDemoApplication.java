package com.demo.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for MySQL Observability Demo.
 *
 * This demo application showcases complete database query observability with:
 * - Prometheus for metrics (queries/sec, latency, etc)
 * - Loki for structured logging
 * - Tempo for distributed tracing
 * - Grafana for visualization
 */
@SpringBootApplication
public class ObservabilityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityDemoApplication.class, args);
        System.out.println("\n" +
            "=======================================================\n" +
            "  MySQL Observability Demo Started!\n" +
            "=======================================================\n" +
            "  Application: http://localhost:8080\n" +
            "  API Docs: http://localhost:8080/api\n" +
            "  Metrics: http://localhost:8080/actuator/prometheus\n" +
            "  Grafana: http://localhost:3000 (admin/admin)\n" +
            "=======================================================\n"
        );
    }
}
