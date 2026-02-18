package com.queue.management.security;

import com.queue.management.entity.CounterStaff;
import com.queue.management.entity.Student;
import com.queue.management.enums.UserType;
import com.queue.management.repository.CounterStaffRepository;
import com.queue.management.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final CounterStaffRepository counterStaffRepository;

    // Spring Security calls this method to load user
    // username = roll number OR staff ID
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // First try to find as student
        Student student = studentRepository
                .findByRollNumber(username)
                .orElse(null);

        if (student != null) {
            log.debug("Found student: {}", username);
            return new SecurityUser(
                    student.getRollNumber(),
                    student.getPassword(),
                    UserType.STUDENT,
                    student.getName()
            );
        }

        // If not student, try counter staff
        CounterStaff staff = counterStaffRepository
                .findByStaffId(username)
                .orElse(null);

        if (staff != null) {
            log.debug("Found counter staff: {}", username);
            return new SecurityUser(
                    staff.getStaffId(),
                    staff.getPassword(),
                    UserType.COUNTER_STAFF,
                    staff.getName()
            );
        }

        // User not found in either table
        log.error("User not found: {}", username);
        throw new UsernameNotFoundException(
                "User not found with identifier: " + username
        );
    }
}