package com.xietg.kc.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc 
class AuthLoginIT extends AbstractPostgresIT {

    @Autowired
    MockMvc mvc;

    @Test
    void login_should_work_with_seeded_user() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "admin@example.com",
                      "password": "admin123"
                    }
                    """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.access_token").isNotEmpty())
           .andExpect(jsonPath("$.role").exists());
    }
}
