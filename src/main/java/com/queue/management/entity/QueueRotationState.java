package com.queue.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_rotation_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueRotationState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_date", unique = true, nullable = false)
    private LocalDate serviceDate;
    
    @Column(name = "last_used_counter_id", nullable = false)
    private Long lastUsedCounterId;
    
    @Version
    private Long version;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}