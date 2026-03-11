package com.apiframework.reporting.allure;

public record HttpReportingConfiguration(
    HttpStepNameStrategy stepNameStrategy,
    HttpAttachmentRenderer attachmentRenderer,
    HttpErrorAttachmentStrategy errorAttachmentStrategy
) {
    private static final String STEP_NAME_STRATEGY_CLASS = "framework.reporting.stepNameStrategyClass";
    private static final String ATTACHMENT_RENDERER_CLASS = "framework.reporting.attachmentRendererClass";
    private static final String MASKING_STRATEGY_CLASS = "framework.reporting.maskingStrategyClass";
    private static final String ERROR_ATTACHMENT_STRATEGY_CLASS = "framework.reporting.errorAttachmentStrategyClass";
    private static final String ATTACHMENT_MAX_BYTES_PROPERTY = "framework.reporting.attachments.maxBytes";

    public static HttpReportingConfiguration defaultConfiguration(boolean attachmentsEnabled) {
        HttpMaskingStrategy maskingStrategy = instantiate(
            System.getProperty(MASKING_STRATEGY_CLASS),
            HttpMaskingStrategy.class,
            DefaultHttpMaskingStrategy::new
        );

        HttpAttachmentRenderer renderer = instantiate(
            System.getProperty(ATTACHMENT_RENDERER_CLASS),
            HttpAttachmentRenderer.class,
            () -> new DefaultHttpAttachmentRenderer(attachmentsEnabled, Integer.getInteger(ATTACHMENT_MAX_BYTES_PROPERTY, 65_536), maskingStrategy)
        );

        return new HttpReportingConfiguration(
            instantiate(System.getProperty(STEP_NAME_STRATEGY_CLASS), HttpStepNameStrategy.class, DefaultHttpStepNameStrategy::new),
            renderer,
            instantiate(System.getProperty(ERROR_ATTACHMENT_STRATEGY_CLASS), HttpErrorAttachmentStrategy.class, DefaultHttpErrorAttachmentStrategy::new)
        );
    }

    private static <T> T instantiate(String className, Class<T> expectedType, java.util.function.Supplier<T> fallback) {
        if (className == null || className.isBlank()) {
            return fallback.get();
        }
        try {
            Class<?> loadedClass = Class.forName(className);
            if (!expectedType.isAssignableFrom(loadedClass)) {
                throw new IllegalArgumentException("Class " + className + " must implement " + expectedType.getName());
            }
            return expectedType.cast(loadedClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate reporting extension: " + className, e);
        }
    }
}
