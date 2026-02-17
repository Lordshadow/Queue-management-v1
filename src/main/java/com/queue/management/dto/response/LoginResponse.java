package com.queue.management.dto.response;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    // JWT token for authentication
    // Frontend stores this and sends it with every request
    private String token;

    // Who logged in? STUDENT or COUNTER_STAFF
    private UserType userType;

    // Full name of the logged in user
    private String name;

    // For students: their roll number
    // For staff: their staff ID
    private String identifier;

    // Only for COUNTER_STAFF
    // Which counter they selected (A or B)
    private CounterName assignedCounter;

    // Success message
    private String message;
}