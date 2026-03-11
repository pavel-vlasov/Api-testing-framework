package com.apiframework.core.filter;

/**
 * SPI interface for pluggable HTTP filter policy discovery.
 *
 * <p>Implementations are loaded via {@link java.util.ServiceLoader} in
 * {@code BaseApiTest#filterPolicy()}.
 * When no provider is found on the classpath, the framework falls back
 * to {@link HttpFilterPolicy#defaultPolicy()}.
 */
public interface FilterPolicyProvider {
    HttpFilterPolicy provide();
}
