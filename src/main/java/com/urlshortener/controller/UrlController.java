package com.urlshortener.controller;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.createShortUrl(
                request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/api/urls/my")
    public ResponseEntity<List<UrlResponse>> getMyUrls(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                urlService.getMyUrls(userDetails.getUsername()));
    }
    
    
}