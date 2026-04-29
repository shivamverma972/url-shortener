package com.urlshortener.service;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service  // tells Spring this is a service layer bean
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    // reads app.base-url from application.yml
    @Value("${app.base-url}")
    private String baseUrl;

    // Constructor injection — best practice over @Autowired
    public UrlService(UrlRepository urlRepository,
                      ShortCodeGenerator shortCodeGenerator) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    // ─── CREATE SHORT URL ────────────────────────────────────────────
    public UrlResponse createShortUrl(CreateUrlRequest request) {

        // Generate a unique short code
        String shortCode = generateUniqueCode();

        // Build and save the Url entity
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);

        Url savedUrl = urlRepository.save(url);

        return mapToResponse(savedUrl);
    }

    // ─── GET ORIGINAL URL FOR REDIRECT ──────────────────────────────
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                    new RuntimeException("Short URL not found: " + shortCode));

        // Increment click count every time someone visits
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    // ─── GET ALL URLs ────────────────────────────────────────────────
    public List<UrlResponse> getAllUrls() {
        return urlRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── DELETE URL ──────────────────────────────────────────────────
    public void deleteUrl(Long id) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("URL not found with id: " + id));
        url.setIsActive(false);   // soft delete
        urlRepository.save(url);
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────

    // Keeps generating codes until we find one that doesn't exist
    private String generateUniqueCode() {
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    // Converts a Url entity into a UrlResponse DTO
    private UrlResponse mapToResponse(Url url) {
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                baseUrl + "/" + url.getShortCode(),  // full short URL
                url.getShortCode(),
                url.getClickCount(),
                url.getCreatedAt()
        );
    }
}