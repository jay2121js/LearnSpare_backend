package com.example.SmartLearn.Controller;

import com.example.SmartLearn.DTO.CourseDTO;
import com.example.SmartLearn.DTO.UserDTO;
import com.example.SmartLearn.Entity.*;
import com.example.SmartLearn.Enum.Role;
import com.example.SmartLearn.Repository.CourseRepo;
import com.example.SmartLearn.Repository.UserRepo;
import com.example.SmartLearn.Service.CourseService;
import com.example.SmartLearn.Service.UserService;
import com.example.SmartLearn.Service.VideoService;
import com.example.SmartLearn.util.Cookies;
import com.example.SmartLearn.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/Public")
public class PublicController {
    private CourseService courseService;
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
   private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepository;
    @Autowired
    private VideoService videoService;
    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    public PublicController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Cookies cookieService;

    @GetMapping("/AuthCourseInstructor")
    public boolean AuthCourseInstructor(@RequestParam("id") Long courseId,@RequestParam("username") String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            if (courseService.isCourseOwnedByTeacher(user.getId(), courseId)) {
                return true;
            }
        }
         return false;
    }
    @GetMapping("/Courses/{CourseName}")
    public List<Course> getByCourseName(@PathVariable String CourseName) {
        return courseService.getCourseByName(CourseName);
    }
    @GetMapping("/course/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id){
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
    @GetMapping("/all/Course")
    public List<CourseDTO> getAllCourses() {
        return courseService.getLiteCourse(); // Make sure this method runs!
    }
    @GetMapping("/FilteredCourses")
    public Page<CourseDTO> listCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 12, sort = "enrolledUsers", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return courseRepo.findBestCoursesByFiltersDTO(category, difficulty, search, pageable);
    }
    @PostMapping("/Instructor")
    public void newInstructor(@RequestBody User teacher) {
        teacher.setRole(Role.TEACHER);
        userService.saveUser(teacher,Role.TEACHER.toString());
    }



    @GetMapping("/ok")
    public ResponseEntity<String> getOk() {
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/Student")
    public void newStudent(@RequestBody User student) {
        student.setRole(Role.STUDENT);
        userService.saveUser(student,Role.STUDENT.toString());
    }

    @PostMapping("/courses")
    public void addCourse(@RequestBody List<Course> ListCourse) {
        String[] u = {"jay21213.js@gmail.com","jay21213.jjss@gmail.com"};
        for (int i = 0; i < ListCourse.size(); i++) {
            Course c = ListCourse.get(i);
            User user = userRepository.findByEmail(u[i%2]);
            if (user != null && user.getTeacher() != null) {
                c.setInstructor(user.getTeacher());
                courseService.addCourse(c);
               log.info("Course added successfully.");
            }
            log.info("Course creation failed.");
        }
    }
    @GetMapping("/video")
    public List<Video> getVideo() {
       return videoService.getAllVideo();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(HttpServletRequest request) {
        String jwt = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) { // Add null check
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwt")) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            return ResponseEntity.status(401).body(null);
        }

        try {
            String email = jwtUtil.extractUsername(jwt);
            User user = userService.findByUsername(email); // Assuming userRepository is userRepo
            if (user == null) {
                return ResponseEntity.status(401).body(null);
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole().toString());
            userData.put("avatar", user.getAvatar());
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/enrolled-ids")
    public List<Long> getEnrolledCourseIds() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        return courseService.getEnrolledCourseIdsForCurrentUser(user.getId());
    }
    @GetMapping("/owned-ids")
    public List<Long> getOwnedCourseIds() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if (user.getRole() == Role.TEACHER) {
            return courseService.getOwnedCourseIdsForCurrentUser(user.getId());

        }
        return new ArrayList<Long>();
    }


    // Optional: check if current user is enrolled in a specific course
    @GetMapping("/is-enrolled/{courseId}")
    public boolean isEnrolled(@PathVariable Long courseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        return courseService.isUserEnrolledInCourse(user.getId(), courseId);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
            User dbUser = userRepository.findByUsername(user.getUsername());
            cookieService.setCookies(dbUser,jwtToken, response);


            return ResponseEntity.ok(new UserResponse(userDetails.getUsername(), dbUser.getAvatar()));
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Invalid username or password");
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO, HttpServletResponse response) {
        try {
            if (userRepository.findByUsername(userDTO.getUsername()) != null) {
                return ResponseEntity.status(400).body("Username already exists");
            }
            if (userDTO.getEmail() != null && userRepository.findByEmail(userDTO.getEmail()) != null) {
                return ResponseEntity.status(400).body("Email already exists");
            }

            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setName(userDTO.getName());
            user.setGender(userDTO.getGender());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setAddress(userDTO.getAddress());
            String roleInput = userDTO.getRole().toUpperCase();
            if (roleInput.equals("STUDENT") || roleInput.equals("ROLE_STUDENT")) {
                user.setRole(Role.STUDENT);
                user.setStudent(new Student());
            } else if (roleInput.equals("TEACHER") || roleInput.equals("ROLE_TEACHER")) {
                user.setRole(Role.TEACHER);
                user.setStudent(new Student());
                user.setTeacher(new Teacher());
            } else {
                return ResponseEntity.status(400).body("Invalid role. Use 'STUDENT' or 'TEACHER'");
            }
            user.setAvatar(null); // No default avatar
            userRepository.save(user);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), userDTO.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
            cookieService.setCookies(user,jwtToken, response);

            return ResponseEntity.ok(new GoogleUserResponse(user.getUsername(), user.getAvatar()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Failed to register: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addHeader("Set-Cookie", "jwt=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax");

        Cookie userCookie = new Cookie("user", "");
        userCookie.setHttpOnly(true);
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        response.addHeader("Set-Cookie", "user=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax");

        return ResponseEntity.ok().build();
    }
}

class UserResponse {
    private String username;
    private String avatar;

    public UserResponse(String username, String avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }
}