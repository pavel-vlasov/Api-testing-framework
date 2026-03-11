package com.apiframework.reporting.allure;

import java.util.Map;

public interface HttpMaskingStrategy {
    Map<String, String> maskHeaders(Map<String, String> headers);

    String maskBody(String body);
}
