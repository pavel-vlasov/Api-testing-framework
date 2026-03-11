package com.apiframework.oracle.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

public final class OracleDslContextFactory {
    private OracleDslContextFactory() {
    }

    public static DSLContext create(DataSource dataSource) {
        return DSL.using(dataSource, SQLDialect.DEFAULT);
    }
}
