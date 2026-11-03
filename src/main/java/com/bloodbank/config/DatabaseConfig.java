package com.bloodbank.config;

import com.bloodbank.util.DatabaseConnection;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    private final DataSource dataSource;

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        DatabaseConnection.setDataSource(dataSource);
    }
}
