package com.apiframework.core.filter;

import io.restassured.filter.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HttpFilterPolicy {
    private final List<Filter> filters;

    private HttpFilterPolicy(List<Filter> filters) {
        this.filters = List.copyOf(filters);
    }

    public static HttpFilterPolicy defaultPolicy() {
        List<Filter> defaultFilters = new ArrayList<>();
        defaultFilters.add(new CorrelationIdFilter());
        defaultFilters.add(new TimingFilter());
        defaultFilters.add(new RequestResponseLoggingFilter());
        return new HttpFilterPolicy(defaultFilters);
    }

    public List<Filter> filters() {
        return Collections.unmodifiableList(filters);
    }

    public HttpFilterPolicy withAdditionalFilter(Filter filter) {
        List<Filter> copy = new ArrayList<>(filters);
        copy.add(filter);
        return new HttpFilterPolicy(copy);
    }
}
