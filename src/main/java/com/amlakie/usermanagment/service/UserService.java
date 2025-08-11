package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.rentalMaintenance.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {
    // For testing purposes only
    private String testRole = null;

    public UserDTO getCurrentUser() {
        // If test role is set (for testing only)
        if (testRole != null) {
            return createTestUser(testRole);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        UserDTO user = new UserDTO();
        user.setName(authentication.getName());
        user.setId(extractUserIdFromAuthentication(authentication));
        user.setRole(extractRoleFromAuthorities(authentication.getAuthorities()));

        return user;
    }

    // TESTING ONLY - to be removed in production
    public void setTestRole(String role) {
        if (!"DRIVER".equals(role) && !"DISTRIBUTOR".equals(role)) {
            throw new IllegalArgumentException("Invalid test role");
        }
        this.testRole = role;
    }

    private UserDTO createTestUser(String role) {
        UserDTO user = new UserDTO();
        user.setId("DRIVER".equals(role) ? 2L : 1L);
        user.setName("Test " + role);
        user.setRole(role);
        return user;
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // Implement your actual ID extraction logic
        return 0L; // Replace with real implementation
    }

    private String extractRoleFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            throw new IllegalStateException("User has no assigned authorities");
        }

        Optional<String> role = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst();

        return role.orElseThrow(() ->
                new IllegalStateException("No valid role authority found"));
    }
}