package com.example.SmartLearn.util;

import com.example.SmartLearn.Entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Cookies {

    @Value("${app.environment}")
    private String environment;

    private boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }

    public void setCookies(User user, String jwt, HttpServletResponse response) {
        int maxAge = 24 * 60 * 60;
        boolean secure = isProduction();

        // JWT cookie (HttpOnly)
        String jwtCookie = buildCookie("jwt", jwt, maxAge, secure, true);
        response.addHeader("Set-Cookie", jwtCookie);
        log.info("Set JWT cookie: {}", jwtCookie);

        try {
            Map<String, String> userData = new HashMap<>();
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole().toString());

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(userData);
            String encodedJson = URLEncoder.encode(jsonString, StandardCharsets.UTF_8);

            // user cookie (NOT HttpOnly so frontend can read it)
            String userCookie = buildCookie("user", encodedJson, maxAge, secure, false);
            response.addHeader("Set-Cookie", userCookie);
            log.info("Set user cookie: {}", userCookie);
        } catch (Exception e) {
            log.error("Failed to serialize user data: {}", e.getMessage(), e);
        }
    }


    public String generateCookies(User user, String jwt, HttpServletResponse response) {
        int maxAge = 24 * 60 * 60;
        boolean secure = isProduction();

        // JWT cookie (HttpOnly)
        String jwtCookie = buildCookie("jwt", jwt, maxAge, secure, true);
        return jwtCookie;
    }

    private String buildCookie(String name, String value, int maxAge, boolean secure, boolean httpOnly) {
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=").append(value)
                .append("; Path=/")
                .append("; Max-Age=").append(maxAge)
                .append("; SameSite=None");

        if (secure) {
            cookie.append("; Secure");
        }
        if (httpOnly) {
            cookie.append("; HttpOnly");
        }
        return cookie.toString();
    }


}
