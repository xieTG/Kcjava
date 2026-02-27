package com.xietg.kc.bootstrap;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.entity.UserRole;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class StartupSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSeeder.class);

    private final AppProperties props;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public StartupSeeder(AppProperties props, UserRepository userRepository, PasswordService passwordService) {
        this.props = props;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // create upload directory
    	Path uploaddirpath = props.getUploadDir();
    	Files.createDirectories(uploaddirpath);

        //  ensure_admin_seed() 
        String adminEmail = props.getAdminEmail().trim().toLowerCase();
        boolean exists = userRepository.findByEmail(adminEmail).isPresent();
        if (!exists) {
            UserEntity admin = new UserEntity();
            admin.setId(UUID.randomUUID());
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordService.hashPassword(props.getAdminPassword()));
            admin.setRole(UserRole.admin);

            userRepository.save(admin);
            log.info("Seeded admin user: {}", adminEmail);
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }
    }
}
