package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;         

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;           

    @Column(name = "click_count")
    private Long clickCount = 0L;      

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;    

    @Column(name = "is_active")
    private Boolean isActive = true;    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")        
    private User user;                   

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.clickCount == null) this.clickCount = 0L;
        if (this.isActive == null) this.isActive = true;
    }
}