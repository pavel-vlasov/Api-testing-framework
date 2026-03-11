package com.apiframework.oracle.config;

public record OracleConnectionConfig(
    String jdbcUrl,
    String username,
    String password,
    int maximumPoolSize
) {
}
