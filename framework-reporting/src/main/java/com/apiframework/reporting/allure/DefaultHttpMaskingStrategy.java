package com.apiframework.reporting.allure;

import com.apiframework.reporting.mask.ReportingMaskingSupport;

import java.util.Map;

public final class DefaultHttpMaskingStrategy implements HttpMaskingStrategy {
    @Override
    public Map<String, String> maskHeaders(Map<String, String> headers) {
        return ReportingMaskingSupport.maskHeaders(headers);
    }

    @Override
    public String maskBody(String body) {
        return ReportingMaskingSupport.maskBody(body);
    }
}
