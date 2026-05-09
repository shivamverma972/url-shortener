package com.urlshortener.service;


import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.model.Url;
import com.urlshortener.model.User;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.urlshortener.exception.ResourceNotFoundException;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlService {
	
	private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CACHE_PREFIX = "url:";

    public UrlService(UrlRepository urlRepository,UserRepository userRepository,ShortCodeGenerator shortCodeGenerator,RedisTemplate<String, String> redisTemplate) {
    			this.urlRepository = urlRepository;
    			this.userRepository = userRepository;
    			this.shortCodeGenerator = shortCodeGenerator;
    			this.redisTemplate = redisTemplate;
    }

    public UrlResponse createShortUrl(CreateUrlRequest request, String userEmail) {
        String shortCode = generateUniqueCode();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);
        url.setUser(user);

        Url savedUrl = urlRepository.save(url);

        redisTemplate.opsForValue().set(
                CACHE_PREFIX + shortCode,
                request.getOriginalUrl(),
                Duration.ofHours(24)
        );
        return mapToResponse(savedUrl);
    }

    public String getOriginalUrl(String shortCode) {

        String cachedUrl = redisTemplate.opsForValue()
                .get(CACHE_PREFIX + shortCode);

        if (cachedUrl != null) {
            updateClickCountAsync(shortCode);
            return cachedUrl;
        }
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Short URL not found: "
                                + shortCode));

        redisTemplate.opsForValue().set(
                CACHE_PREFIX + shortCode,
                url.getOriginalUrl(),
                Duration.ofHours(24)
        );

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }
    public List<UrlResponse> getAllUrls() {
        return urlRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteUrl(Long id) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("URL not found: " + id));

        url.setIsActive(false);
        urlRepository.save(url);

        redisTemplate.delete(CACHE_PREFIX + url.getShortCode());
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private void updateClickCountAsync(String shortCode) {
        urlRepository.findByShortCode(shortCode).ifPresent(url -> {
            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);
        });
    }

    private UrlResponse mapToResponse(Url url) {
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                baseUrl + "/" + url.getShortCode(),
                url.getShortCode(),
                url.getClickCount(),
                url.getCreatedAt()
        );
    }
    
    public List<UrlResponse> getMyUrls(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        return urlRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    
    
}