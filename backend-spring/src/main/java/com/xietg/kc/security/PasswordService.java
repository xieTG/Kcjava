package com.xietg.kc.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.xietg.kc.log.Log;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hashPassword(String rawPassword) {
    	Log.debug(rawPassword);
        return encoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword, String passwordHash) {
    	Log.debug(rawPassword);
    	Log.debug(passwordHash);
        return encoder.matches(rawPassword, passwordHash);
    }
}
