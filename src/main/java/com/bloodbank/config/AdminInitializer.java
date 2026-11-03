package com.bloodbank.config;

import com.bloodbank.dao.AdminDAO;
import com.bloodbank.model.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminDAO adminDAO = new AdminDAO();
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String configuredEmail;

    @Value("${app.admin.password}")
    private String configuredPassword;

    @Value("${app.admin.name:System Administrator}")
    private String configuredName;

    public AdminInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        int existingCount = adminDAO.countAdmins();
        if (existingCount == 0) {
            String email = System.getenv("ADMIN_EMAIL") != null
                    ? System.getenv("ADMIN_EMAIL").trim()
                    : configuredEmail.trim();

            String password = System.getenv("ADMIN_PASSWORD") != null
                    ? System.getenv("ADMIN_PASSWORD").trim()
                    : configuredPassword.trim();

            String name = System.getenv("ADMIN_NAME") != null
                    ? System.getenv("ADMIN_NAME").trim()
                    : configuredName.trim();

            String passwordHash = passwordEncoder.encode(password);

            Admin defaultAdmin = new Admin(0, name, email, passwordHash, LocalDateTime.now());
            boolean created = adminDAO.createAdmin(defaultAdmin);

            if (created) {
                logger.info("==================================================================");
                logger.info(" Default admin created successfully: {}", email);
                logger.info("==================================================================");
            } else {
                logger.warn("Could not seed default admin. Please verify database connection.");
            }
        } else {
            logger.info("Admin accounts already exist (count: {}). Skipping admin seed.", existingCount);
        }
    }
}
