package com.example.iskolarphh.api;

/**
 * Configuration data class for API client settings.
 * Follows SOLID principles by encapsulating configuration parameters.
 */
public class ApiClientConfig {
    
    private final String baseUrl;
    private final String apiKey;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final boolean enableLogging;
    
    public ApiClientConfig(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, 30000L, 30000L, true);
    }
    
    public ApiClientConfig(String baseUrl, String apiKey, long connectTimeoutMs, long readTimeoutMs, boolean enableLogging) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        if (connectTimeoutMs <= 0) {
            throw new IllegalArgumentException("Connect timeout must be positive");
        }
        if (readTimeoutMs <= 0) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.enableLogging = enableLogging;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    public long getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    public boolean isLoggingEnabled() {
        return enableLogging;
    }
    
    @Override
    public String toString() {
        return "ApiClientConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", apiKey='[REDACTED]'" +
                ", connectTimeoutMs=" + connectTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", enableLogging=" + enableLogging +
                '}';
    }
}
