package com.example.SmartLearn.Controller;

import com.example.SmartLearn.DTO.CourseDTO;
import com.example.SmartLearn.DTO.UserDTO;
import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Entity.Video;
import com.example.SmartLearn.Repository.CourseRepo;
import com.example.SmartLearn.Service.CourseService;
import com.example.SmartLearn.Service.UserService;
import com.example.SmartLearn.Service.VideoService;
import com.example.SmartLearn.util.NullPropertis;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/Student")
public class StudentController {

    private NullPropertis nullPropertis;
    private UserService userService;
    @Autowired
    VideoService videoService;
    @Autowired
    CourseService courseService;
    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    public StudentController(NullPropertis nullPropertis, UserService userService) {
        this.nullPropertis = nullPropertis;
        this.userService = userService;
    }

    @PutMapping("/update")
    public void updateStudent(@RequestBody UserDTO user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User old = userService.findByUsername(auth.getName());
        BeanUtils.copyProperties(user, old, nullPropertis.getNullProperty(user));
        if (user.getPassword()!=null) {
            userService.update(old);
        }else{
            userService.save(old);
        }
    }

    @GetMapping("/details")
    public User getStudentByUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }


    @DeleteMapping("/delete")
    public void deleteStudentByUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.deleteUser(auth.getName());
    }

    @PostMapping("/Enroll-In/{cId}")
    public void getEnrolledCourses(@PathVariable Long cId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.EnrollCourse(auth.getName(), cId);
    }

    @GetMapping("/play/{vid}")
    public String play(@PathVariable Long vid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getByVideoId(vid);
        if (user.getStudent().getCourses().contains(course))
            return  videoService.getVideoById(vid).getHlsUrl();
        return null;
    }
    @GetMapping("/My-Courses")
    public List<CourseDTO> getEnrolledCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Long userId = userService.findByUsername(username).getId();
        return courseRepo.findEnrolledCoursesByUserId(userId);
    }


}



