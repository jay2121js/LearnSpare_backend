package com.example.SmartLearn.Controller;

import com.example.SmartLearn.Entity.Student;
import com.example.SmartLearn.Entity.Teacher;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Enum.Role;
import com.example.SmartLearn.Repository.UserRepo;
import com.example.SmartLearn.Service.AvatarUploadService;
import com.example.SmartLearn.util.Cookies;
import com.example.SmartLearn.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {
    @Value("${app.environment}")
    private String environment;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${Frontend_URI}")
    private String frontEndUrl;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AvatarUploadService avatarUploadService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Cookies cookies;

    public Map<String, Object> GoogleAuthHandler(String code, String redirectUri) {
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get access token: {}", tokenResponse.getStatusCode());
                return null;
            }

            String idToken = (String) tokenResponse.getBody().get("id_token");
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to fetch user info: {}", userInfoResponse.getStatusCode());
                return null;
            }

            return userInfoResponse.getBody();
        } catch (Exception e) {
            log.error("OAuth processing error: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    @PostMapping("/signup/callback")
    public ResponseEntity<GoogleUserResponse> googleSignupCallback(
            @RequestParam("code") String code,
            @RequestParam("role") String role,
            HttpServletResponse response
    ) {
        try {
            Map<String, Object> userInfo = GoogleAuthHandler(code, frontEndUrl+"/signup");
            if (userInfo == null || !userInfo.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new GoogleUserResponse(null, null, "Failed to fetch user info from Google"));
            }

            String email = userInfo.get("email").toString();
            User existingUser = userRepo.findByEmail(email);
            if (existingUser != null) {
                String jwt = jwtUtil.generateToken(email);
                cookies.setCookies(existingUser, jwt, response);
                return ResponseEntity.ok(new GoogleUserResponse(existingUser.getUsername(), existingUser.getAvatar()));
            }

            String name = userInfo.get("name").toString();
            String picture = userInfo.containsKey("picture") ? userInfo.get("picture").toString() : null;

            User user = new User();
            user.setUsername(email);
            user.setEmail(email);
            user.setName(name);
            user.setPassword(passwordEncoder.encode("google_oauth_" + email));

            if (role.equalsIgnoreCase("STUDENT")) {
                user.setRole(Role.STUDENT);
                Student student = new Student();
                student.setUser(user);
                user.setStudent(student);
            } else if (role.equalsIgnoreCase("TEACHER")) {
                user.setRole(Role.TEACHER);
                Teacher teacher = new Teacher();
                Student student = new Student();
                student.setUser(user);
                teacher.setUser(user);
                user.setStudent(student);
                user.setTeacher(teacher);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GoogleUserResponse(null, null, "Invalid role specified"));
            }
            user.setAvatar("");
            userRepo.save(user);

            String jwt = jwtUtil.generateToken(email);
            cookies.setCookies(user, jwt, response);

            if (picture != null && !picture.isBlank()) {
                avatarUploadService.uploadAvatarAsync(user.getId(), picture);
            }

            return ResponseEntity.ok(new GoogleUserResponse(user.getUsername(), user.getAvatar()));
        } catch (Exception e) {
            // Log the error and rethrow to ensure transaction rollback
            log.error("Signup failed: {}", e.getMessage(), e);
            return null;
        }
    }
    @PostMapping("/login/callback")
    public ResponseEntity<GoogleUserResponse> googleLoginCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        log.debug("Processing Google login callback with code: {}", code);
        Map<String, Object> userInfo = GoogleAuthHandler(code, frontEndUrl+"/login");
        if (userInfo == null || !userInfo.containsKey("email")) {
            log.warn("Invalid user info received from Google");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new GoogleUserResponse(null, null, "Failed to fetch user info from Google"));
        }

        String email = userInfo.get("email").toString();
        String picture = userInfo.containsKey("picture") ? userInfo.get("picture").toString() : null;

        User user = userRepo.findByEmail(email);
        if (user == null) {
            log.info("No user found with email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new GoogleUserResponse(null, null, "No account found. Please sign up first."));
        }

        String jwt = jwtUtil.generateToken(email);
        cookies.setCookies(user,jwt, response);

        if (picture != null && !picture.isBlank() && (user.getAvatar() == null || user.getAvatar().isBlank())) {
            avatarUploadService.uploadAvatarAsync(user.getId(), picture);
        }

        return ResponseEntity.ok(new GoogleUserResponse(user.getUsername(), user.getAvatar()));
    }
}

class GoogleUserResponse {
    private String username;
    private String avatar;
    private String error;

    public GoogleUserResponse(String username, String avatar) {
        this(username, avatar, null);
    }

    public GoogleUserResponse(String username, String avatar, String error) {
        this.username = username;
        this.avatar = avatar;
        this.error = error;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getError() {
        return error;
    }
}