package com.queue.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "daily_counter_states",
       uniqueConstraints = @UniqueConstraint(columnNames = {"counter_id", "service_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCounterState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id", nullable = false)
    private ServiceCounter counter;
    
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;
    
    @Column(name = "last_token_number", nullable = false)
    @Builder.Default
    private Integer lastTokenNumber = 0;
    
    @Version
    private Long version;
}