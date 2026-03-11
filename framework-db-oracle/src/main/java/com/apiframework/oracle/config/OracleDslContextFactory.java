package com.apiframework.oracle.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class OracleDslContextFactory {
    private OracleDslContextFactory() {
    }

    public static DSLContext create(DataSource dataSource) {
        try {
            return DSL.using(dataSource.getConnection(), SQLDialect.DEFAULT);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create jOOQ DSL context", exception);
        }
    }
}
