package com.apiframework.oracle.repository;

import com.apiframework.oracle.model.DbOrder;
import org.awaitility.Awaitility;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class OrderRepository {
    private final DSLContext dslContext;

    public OrderRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Optional<DbOrder> findById(long orderId) {
        Record record = dslContext
            .select(
                DSL.field("ID", Long.class),
                DSL.field("USER_ID", Long.class),
                DSL.field("STATUS", String.class)
            )
            .from(DSL.table("ORDERS"))
            .where(DSL.field("ID").eq(orderId))
            .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        return Optional.of(new DbOrder(
            record.get("ID", Long.class),
            record.get("USER_ID", Long.class),
            record.get("STATUS", String.class)
        ));
    }

    public DbOrder awaitStatus(long orderId, String expectedStatus, Duration timeout, Duration pollInterval) {
        AtomicReference<DbOrder> captured = new AtomicReference<>();

        Awaitility.await("Wait for order status")
            .atMost(timeout)
            .pollInterval(pollInterval)
            .until(() -> {
                Optional<DbOrder> order = findById(orderId);
                if (order.isPresent() && expectedStatus.equalsIgnoreCase(order.get().status())) {
                    captured.set(order.get());
                    return true;
                }
                return false;
            });

        return captured.get();
    }
}
