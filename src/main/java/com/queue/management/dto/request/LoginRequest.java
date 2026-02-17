package com.queue.management.dto.request;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    // Roll number for student OR staff ID for counter staff
    @NotBlank(message = "Identifier is required")
    private String identifier;

    // Password
    @NotBlank(message = "Password is required")
    private String password;

    // Who is logging in? STUDENT or COUNTER_STAFF
    @NotNull(message = "User type is required")
    private UserType userType;

    // Only required for COUNTER_STAFF
    // Which counter are they operating? A or B
    private CounterName selectedCounter;
}