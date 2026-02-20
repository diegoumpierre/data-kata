package com.demo.observability.observability;

import io.micrometer.core.instrument.*;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for recording query observability metrics.
 * Integrates with Prometheus, Loki, and Tempo for complete observability.
 */
@Component
public class QueryObservabilityService {

    private static final Logger logger = LoggerFactory.getLogger("query-observability");

    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    public QueryObservabilityService(MeterRegistry meterRegistry, Tracer tracer) {
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;

        // Register gauge for unique query locations
        meterRegistry.gauge("db.queries.unique_locations", this, service -> 0);
    }

    /**
     * Records a database query execution with full observability.
     *
     * @param query The SQL query executed
     * @param executionTimeMs Execution time in milliseconds
     * @param caller Stack trace element indicating where the query was called from
     */
    public void recordQuery(String query, long executionTimeMs, StackTraceElement caller) {
        String normalizedQuery = normalizeQuery(query);
        String queryType = extractQueryType(query);
        String location = formatLocation(caller);

        // 1. PROMETHEUS: Record metrics
        recordMetrics(normalizedQuery, queryType, location, executionTimeMs);

        // 2. LOKI: Log with structured context
        logStructuredQuery(query, normalizedQuery, queryType, location, executionTimeMs);

        // 3. TEMPO: Add span to current trace
        recordTrace(normalizedQuery, queryType, location, executionTimeMs);
    }

    private void recordMetrics(String normalizedQuery, String queryType, String location, long executionTimeMs) {
        // Counter: total queries by type and location
        Counter.builder("db.queries.total")
                .tag("query_type", queryType)
                .tag("location", location)
                .description("Total number of database queries executed")
                .register(meterRegistry)
                .increment();

        // Timer: query latency with percentiles
        Timer.builder("db.query.duration")
                .tag("query_type", queryType)
                .tag("location", location)
                .description("Database query execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(executionTimeMs, TimeUnit.MILLISECONDS);

        // Distribution summary: query execution time
        DistributionSummary.builder("db.query.execution_time_ms")
                .tag("normalized_query", truncate(normalizedQuery, 100))
                .tag("location", location)
                .description("Query execution time distribution")
                .baseUnit("milliseconds")
                .register(meterRegistry)
                .record(executionTimeMs);
    }

    private void logStructuredQuery(String query, String normalizedQuery, String queryType,
                                     String location, long executionTimeMs) {
        // Structured logging for Loki
        Map<String, Object> logContext = new HashMap<>();
        logContext.put("query", truncate(query, 500));
        logContext.put("normalized_query", truncate(normalizedQuery, 500));
        logContext.put("query_type", queryType);
        logContext.put("execution_time_ms", executionTimeMs);
        logContext.put("location", location);
        logContext.put("slow_query", executionTimeMs > 100);

        if (executionTimeMs > 100) {
            logger.warn("Slow query detected: {} ms | {} | {}",
                executionTimeMs, location, normalizedQuery);
        } else {
            logger.debug("Query executed: {} ms | {} | {}",
                executionTimeMs, location, normalizedQuery);
        }
    }

    private void recordTrace(String normalizedQuery, String queryType, String location, long executionTimeMs) {
        // Add span to distributed trace (Tempo)
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("db.system", "mysql");
            currentSpan.tag("db.operation", queryType);
            currentSpan.tag("db.statement", truncate(normalizedQuery, 500));
            currentSpan.tag("code.location", location);
            currentSpan.tag("db.execution_time_ms", String.valueOf(executionTimeMs));
        }
    }

    /**
     * Normalizes a SQL query by replacing literals with placeholders.
     * This groups similar queries together for better metrics aggregation.
     */
    private String normalizeQuery(String query) {
        return query.replaceAll("'[^']*'", "'?'")
                    .replaceAll("\\b\\d+\\b", "?")
                    .replaceAll("\\s+", " ")
                    .trim();
    }

    /**
     * Extracts the query type (SELECT, INSERT, UPDATE, DELETE, etc.)
     */
    private String extractQueryType(String query) {
        String upperQuery = query.trim().toUpperCase();
        if (upperQuery.startsWith("SELECT")) return "SELECT";
        if (upperQuery.startsWith("INSERT")) return "INSERT";
        if (upperQuery.startsWith("UPDATE")) return "UPDATE";
        if (upperQuery.startsWith("DELETE")) return "DELETE";
        if (upperQuery.startsWith("CREATE")) return "CREATE";
        if (upperQuery.startsWith("DROP")) return "DROP";
        if (upperQuery.startsWith("ALTER")) return "ALTER";
        return "OTHER";
    }

    /**
     * Formats stack trace element into readable location string
     */
    private String formatLocation(StackTraceElement caller) {
        String className = caller.getClassName();
        // Remove package prefix, keep only class name
        className = className.substring(className.lastIndexOf('.') + 1);

        return String.format("%s.%s:%d",
            className,
            caller.getMethodName(),
            caller.getLineNumber());
    }

    /**
     * Truncates string to max length
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
