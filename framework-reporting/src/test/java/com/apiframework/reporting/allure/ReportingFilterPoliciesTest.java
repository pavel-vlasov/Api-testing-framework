package com.apiframework.reporting.allure;

import com.apiframework.core.filter.HttpFilterPolicy;
import io.restassured.filter.Filter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportingFilterPoliciesTest {
    @AfterMethod
    public void cleanupProperties() {
        System.clearProperty(ReportingFilterPolicies.HTTP_STEPS_ENABLED_PROPERTY);
        System.clearProperty(ReportingFilterPolicies.ATTACHMENTS_ENABLED_PROPERTY);
    }

    @Test
    public void shouldEnableAllureHttpFilterByDefault() {
        HttpFilterPolicy policy = ReportingFilterPolicies.withAllureReporting();

        assertThat(policy.filters())
            .anyMatch(filter -> filter.getClass().equals(AllureHttpStepFilter.class));
    }

    @Test
    public void shouldDisableHttpStepFilterWhenPropertyIsFalse() {
        System.setProperty(ReportingFilterPolicies.HTTP_STEPS_ENABLED_PROPERTY, "false");

        HttpFilterPolicy policy = ReportingFilterPolicies.withAllureReporting();

        assertThat(policy.filters())
            .noneMatch(filter -> filter.getClass().equals(AllureHttpStepFilter.class));
    }

    @Test
    public void shouldRespectAttachmentPropertyWithoutRemovingHttpStepFilter() {
        System.setProperty(ReportingFilterPolicies.ATTACHMENTS_ENABLED_PROPERTY, "false");

        HttpFilterPolicy policy = ReportingFilterPolicies.withAllureReporting();

        Filter filter = policy.filters().stream()
            .filter(it -> it.getClass().equals(AllureHttpStepFilter.class))
            .findFirst()
            .orElseThrow();

        assertThat(filter).isNotNull();
    }
}
