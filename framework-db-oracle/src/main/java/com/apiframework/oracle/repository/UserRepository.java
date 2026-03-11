package com.apiframework.oracle.repository;

import com.apiframework.oracle.model.DbUser;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.Optional;

public final class UserRepository {
    private final DSLContext dslContext;

    public UserRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Optional<DbUser> findById(long userId) {
        Record record = dslContext
            .select(
                DSL.field("ID", Long.class),
                DSL.field("EMAIL", String.class),
                DSL.field("STATUS", String.class)
            )
            .from(DSL.table("USERS"))
            .where(DSL.field("ID").eq(userId))
            .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        return Optional.of(new DbUser(
            record.get("ID", Long.class),
            record.get("EMAIL", String.class),
            record.get("STATUS", String.class)
        ));
    }
}
