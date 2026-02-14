package com.queue.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "counter_break_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounterBreakLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id", nullable = false)
    private ServiceCounter counter;
    
    @Column(name = "break_start", nullable = false)
    private LocalDateTime breakStart;
    
    @Column(name = "break_end")
    private LocalDateTime breakEnd;
    
    @Column(length = 255)
    private String reason;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    @Column(name = "actual_duration")
    private Integer actualDuration;
}