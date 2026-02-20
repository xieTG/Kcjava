package com.xietg.kc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Secret used to sign JWTs (HS256).
     * Mirrors the Python backend's JWT_SECRET env var.
     */
    private String jwtSecret = "dev_secret_change_me";

    private String adminEmail = "admin@example.com";
    private String adminPassword = "admin123";

    /**
     * Directory where uploaded XLSX files are stored.
     * Default matches docker-compose volume (/data/uploads).
     */
    private Path uploadDir = Path.of("/data/uploads");

    /**
     * Comma-separated list of allowed CORS origins.
     * Example: "http://localhost:3000,http://127.0.0.1:3000"
     */
    private String corsOrigins = "http://localhost:3000";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(Path uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }
}
