package com.demo.observability.config;

import com.demo.observability.observability.QueryObservabilityService;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * DataSource configuration that wraps the MySQL connection with observability proxy.
 * All queries are intercepted and sent to QueryObservabilityService for metrics collection.
 */
@Configuration
public class DataSourceConfig {

    private final QueryObservabilityService observabilityService;

    public DataSourceConfig(QueryObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        // Create original MySQL DataSource
        DataSource originalDataSource = properties.initializeDataSourceBuilder().build();

        // Wrap with proxy for observability
        return ProxyDataSourceBuilder
                .create(originalDataSource)
                .name("MySQL-Observable")
                .afterQuery((execInfo, queryInfoList) -> {
                    // Find application caller (skip framework classes)
                    StackTraceElement caller = findApplicationCaller(
                        Thread.currentThread().getStackTrace()
                    );

                    // Record each query executed
                    queryInfoList.forEach(queryInfo -> {
                        String query = queryInfo.getQuery();
                        long timeMs = execInfo.getElapsedTime();

                        observabilityService.recordQuery(query, timeMs, caller);
                    });
                })
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Finds the first stack trace element that belongs to application code
     * (not framework or infrastructure code).
     */
    private StackTraceElement findApplicationCaller(StackTraceElement[] stack) {
        for (StackTraceElement element : stack) {
            String className = element.getClassName();

            // Skip framework and infrastructure classes
            if (!className.startsWith("java.") &&
                !className.startsWith("javax.") &&
                !className.startsWith("jdk.") &&
                !className.startsWith("sun.") &&
                !className.startsWith("net.ttddyy.") &&
                !className.startsWith("com.mysql.") &&
                !className.startsWith("com.zaxxer.hikari.") &&
                !className.startsWith("org.springframework.jdbc.") &&
                !className.startsWith("org.springframework.aop.") &&
                !className.startsWith("org.springframework.transaction.") &&
                !className.contains("$$EnhancerBy") &&
                !className.contains("$$SpringCGLIB$$") &&
                !className.contains("$Proxy")) {

                return element;
            }
        }

        // Fallback to first element if no application code found
        return stack[0];
    }
}
