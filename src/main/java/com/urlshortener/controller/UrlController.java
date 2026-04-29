package com.urlshortener.controller;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController  // handles HTTP requests and returns JSON
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    // POST /api/urls — create a short URL
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /{shortCode} — redirect to original URL
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        // 302 redirect — browser goes to original URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    // GET /api/urls — get all URLs
    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    // DELETE /api/urls/{id} — soft delete a URL
    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.noContent().build();
    }
}