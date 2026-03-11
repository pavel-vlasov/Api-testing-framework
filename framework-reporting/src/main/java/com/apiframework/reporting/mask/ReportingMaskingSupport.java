package com.apiframework.reporting.mask;

import com.apiframework.core.filter.SensitiveDataMasker;

import java.util.Map;

public final class ReportingMaskingSupport {
    private ReportingMaskingSupport() {
    }

    public static Map<String, String> maskHeaders(Map<String, String> headers) {
        return SensitiveDataMasker.maskHeaders(headers);
    }

    public static String maskBody(String body) {
        return SensitiveDataMasker.maskJsonLikeBody(body);
    }
}
