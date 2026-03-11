package com.apiframework.reporting.allure;

import com.apiframework.core.filter.FilterPolicyProvider;
import com.apiframework.core.filter.HttpFilterPolicy;

/**
 * ServiceLoader-discovered provider that activates Allure HTTP reporting filters.
 */
public class AllureFilterPolicyProvider implements FilterPolicyProvider {

    @Override
    public HttpFilterPolicy provide() {
        return ReportingFilterPolicies.withAllureReporting();
    }
}
