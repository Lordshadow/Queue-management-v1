package com.queue.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private String rollNumber;
    private String name;
    private String email;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
}
