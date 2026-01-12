package com.nghiashop.ecome_backend.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String username = System.getenv("PGUSER");
        String password = System.getenv("PGPASSWORD");

        if (databaseUrl != null && databaseUrl.contains("@")) {
            try {
                String urlWithoutProtocol = databaseUrl.substring(databaseUrl.indexOf("://") + 3);

                if (urlWithoutProtocol.contains("@")) {
                    String[] parts = urlWithoutProtocol.split("@");
                    String credentialsPart = parts[0];
                    String hostPart = parts[1];

                    if (username == null && credentialsPart.contains(":")) {
                        String[] credentials = credentialsPart.split(":", 2);
                        username = credentials[0];
                        password = credentials[1];
                    }

                    databaseUrl = "jdbc:postgresql://" + hostPart;
                }
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            databaseUrl = "jdbc:" + databaseUrl;
        }

        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/hotelmanagement";
            username = "postgres";
            password = "postgres";
        }

        System.out.println("=== Database Connection Info ===");
        System.out.println("URL: " + databaseUrl);
        System.out.println("Username: " + username);
        System.out.println("Password: " + (password != null ? "***" : "null"));
        System.out.println("================================");

        return DataSourceBuilder.create()
                .url(databaseUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}