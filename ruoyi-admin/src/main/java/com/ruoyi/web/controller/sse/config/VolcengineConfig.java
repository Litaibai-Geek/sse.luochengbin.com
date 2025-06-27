package com.ruoyi.web.controller.sse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "volcengine.ark")
public class VolcengineConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
    private String mode;
    private Integer timeout;
    private Integer connectTimeout;
    private Integer connectionPoolSize;
    private Integer connectionPoolKeepAlive;

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(Integer connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public Integer getConnectionPoolKeepAlive() {
        return connectionPoolKeepAlive;
    }

    public void setConnectionPoolKeepAlive(Integer connectionPoolKeepAlive) {
        this.connectionPoolKeepAlive = connectionPoolKeepAlive;
    }
} 