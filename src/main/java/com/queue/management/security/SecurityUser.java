package com.queue.management.security;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
public class SecurityUser implements UserDetails {

    // Username (roll number for student, staff ID for staff)
    private final String username;

    // Hashed password
    private final String password;

    // Who is this? STUDENT or COUNTER_STAFF
    private final UserType userType;

    // Only for COUNTER_STAFF - which counter they operate
    private final CounterName assignedCounter;

    // Full name of the user
    private final String name;

    // Constructor for STUDENT
    public SecurityUser(String username,
                       String password,
                       UserType userType,
                       String name) {
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.name = name;
        this.assignedCounter = null;
    }

    // Constructor for COUNTER_STAFF
    public SecurityUser(String username,
                       String password,
                       UserType userType,
                       String name,
                       CounterName assignedCounter) {
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.name = name;
        this.assignedCounter = assignedCounter;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Give role based on user type
        // STUDENT → ROLE_STUDENT
        // COUNTER_STAFF → ROLE_COUNTER_STAFF
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
