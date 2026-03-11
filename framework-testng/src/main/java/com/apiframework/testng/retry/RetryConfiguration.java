package com.apiframework.testng.retry;

public final class RetryConfiguration {
    private static final String PREDICATE_CLASS_PROPERTY = "test.retry.predicateClass";
    private static final String DELAY_STRATEGY_CLASS_PROPERTY = "test.retry.delayStrategyClass";

    private RetryConfiguration() {
    }

    public static RetryRuntimePolicy globalPolicy() {
        int maxRetries = Integer.parseInt(
            System.getProperty("test.retry.maxRetries", System.getProperty("test.retry.maxAttempts", "1"))
        );
        long delayMs = Long.parseLong(System.getProperty("test.retry.delayMs", "0"));
        return new RetryRuntimePolicy(maxRetries, delayMs);
    }

    public static RetryPredicate retryPredicate() {
        return instantiate(
            System.getProperty(PREDICATE_CLASS_PROPERTY),
            RetryPredicate.class,
            DefaultRetryPredicate::new
        );
    }

    public static RetryDelayStrategy retryDelayStrategy() {
        return instantiate(
            System.getProperty(DELAY_STRATEGY_CLASS_PROPERTY),
            RetryDelayStrategy.class,
            FixedRetryDelayStrategy::new
        );
    }

    private static <T> T instantiate(String configuredClassName, Class<T> expectedType, java.util.function.Supplier<T> fallback) {
        if (configuredClassName == null || configuredClassName.isBlank()) {
            return fallback.get();
        }

        try {
            Class<?> loadedClass = Class.forName(configuredClassName);
            if (!expectedType.isAssignableFrom(loadedClass)) {
                throw new IllegalArgumentException("Class " + configuredClassName + " must implement " + expectedType.getName());
            }
            Object instance = loadedClass.getDeclaredConstructor().newInstance();
            return expectedType.cast(instance);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate retry extension: " + configuredClassName, e);
        }
    }
}
