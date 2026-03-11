package com.apiframework.oracle.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class OracleDataSourceFactory {
    private OracleDataSourceFactory() {
    }

    public static DataSource create(OracleConnectionConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.jdbcUrl());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setMaximumPoolSize(config.maximumPoolSize());
        hikariConfig.setPoolName("api-test-framework-oracle");
        return new HikariDataSource(hikariConfig);
    }
}
