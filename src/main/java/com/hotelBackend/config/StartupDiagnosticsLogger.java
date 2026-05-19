package com.hotelBackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Log de diagnóstico para confirmar a qué BD se conecta realmente el backend.
 * Útil para descartar el clásico problema: "el backend apunta a otra BD distinta".
 */
@Component
@Slf4j
public class StartupDiagnosticsLogger implements ApplicationRunner {

    private final Environment environment;
    private final DataSource dataSource;

    public StartupDiagnosticsLogger(Environment environment, DataSource dataSource) {
        this.environment = environment;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String configuredUrl = environment.getProperty("spring.datasource.url");
            String configuredUser = environment.getProperty("spring.datasource.username");
            String activeProfiles = String.join(",", environment.getActiveProfiles());

            log.info("[DIAGNOSTICS] activeProfiles={} spring.datasource.url={} spring.datasource.username={}",
                    (activeProfiles.isBlank() ? "default" : activeProfiles),
                    configuredUrl,
                    configuredUser
            );

            try (Connection connection = dataSource.getConnection()) {
                log.info("[DIAGNOSTICS] connectedDatabaseUrl={} connectedUser={}", connection.getMetaData().getURL(), connection.getMetaData().getUserName());
            }
        } catch (Exception e) {
            log.warn("[DIAGNOSTICS] No se pudo imprimir información del datasource: {}", e.getMessage());
        }
    }
}

