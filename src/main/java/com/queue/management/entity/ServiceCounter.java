package com.queue.management.entity;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.CounterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_counters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCounter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private CounterName name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CounterStatus status = CounterStatus.ACTIVE;
    
    @Column(name = "daily_limit", nullable = false)
    @Builder.Default
    private Integer dailyLimit = 75;
    
    @Column(name = "break_started_at")
    private LocalDateTime breakStartedAt;
    
    @Column(name = "break_reason")
    private String breakReason;
    
    @Column(name = "estimated_break_duration")
    private Integer estimatedBreakDuration;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}