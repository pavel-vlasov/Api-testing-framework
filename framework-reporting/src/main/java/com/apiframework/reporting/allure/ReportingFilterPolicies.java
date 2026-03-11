package com.apiframework.reporting.allure;

import com.apiframework.core.filter.HttpFilterPolicy;

public final class ReportingFilterPolicies {
    public static final String HTTP_STEPS_ENABLED_PROPERTY = "framework.reporting.httpSteps.enabled";
    public static final String ATTACHMENTS_ENABLED_PROPERTY = "framework.reporting.attachments.enabled";

    private ReportingFilterPolicies() {
    }

    public static HttpFilterPolicy withAllureReporting() {
        if (!isEnabled(HTTP_STEPS_ENABLED_PROPERTY, true)) {
            return HttpFilterPolicy.defaultPolicy();
        }

        boolean attachmentsEnabled = isEnabled(ATTACHMENTS_ENABLED_PROPERTY, true);
        return HttpFilterPolicy.defaultPolicy().withAdditionalFilter(new AllureHttpStepFilter(attachmentsEnabled));
    }

    /**
     * @deprecated use {@link #withAllureReporting()}.
     */
    @Deprecated(forRemoval = false)
    public static HttpFilterPolicy withAllureAttachments() {
        return withAllureReporting();
    }

    private static boolean isEnabled(String propertyName, boolean defaultValue) {
        return Boolean.parseBoolean(System.getProperty(propertyName, String.valueOf(defaultValue)));
    }
}
