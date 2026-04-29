package com.urlshortener.dto.response;

import java.time.LocalDateTime;

public class UrlResponse {

    private Long id;
    private String originalUrl;
    private String shortUrl;       // full URL e.g. http://localhost:8080/abc123
    private String shortCode;      // just the code e.g. abc123
    private Long clickCount;
    private LocalDateTime createdAt;

    // Constructor
    public UrlResponse(Long id, String originalUrl, String shortUrl,
                       String shortCode, Long clickCount, LocalDateTime createdAt) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortUrl() { return shortUrl; }
    public String getShortCode() { return shortCode; }
    public Long getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}