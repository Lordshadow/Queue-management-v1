package com.queue.management.entity;

import com.queue.management.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id", nullable = false)
    private ServiceCounter counter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;
    
    @Column(name = "token_code", unique = true, nullable = false, length = 10)
    private String tokenCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TokenStatus status = TokenStatus.WAITING;
    
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "served_at")
    private LocalDateTime servedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "is_rescheduled")
    @Builder.Default
    private Boolean isRescheduled = false;
    
    @Column(name = "original_service_date")
    private LocalDate originalServiceDate;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}