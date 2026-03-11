package com.apiframework.testng.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RetrySetting {
    int maxRetries() default -1;

    /**
     * @deprecated Use maxRetries. Preserved for compatibility.
     */
    @Deprecated
    int maxAttempts() default -1;

    long delayMs() default -1;
}
