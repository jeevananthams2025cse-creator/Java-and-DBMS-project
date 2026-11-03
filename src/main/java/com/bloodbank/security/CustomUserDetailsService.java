package com.bloodbank.security;

import com.bloodbank.dao.AdminDAO;
import com.bloodbank.model.Admin;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final LoginAttemptService loginAttemptService;

    public CustomUserDetailsService(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(email)) {
            throw new LockedException("Too many failed login attempts. Please try again later.");
        }

        Admin admin = adminDAO.findByEmail(email);
        if (admin == null) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        return new User(
                admin.getEmail(),
                admin.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
