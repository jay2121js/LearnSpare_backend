package com.example.SmartLearn.Controller;

import com.example.SmartLearn.DTO.CourseDTO;
import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Repository.VideoRepo;
import com.example.SmartLearn.Service.CourseService;
import com.example.SmartLearn.Service.UserService;
import com.example.SmartLearn.Service.VideoService;
import com.example.SmartLearn.util.NullPropertis;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/Courses")
public class CourseController {
    private final VideoService videoService;
    private final VideoRepo videoRepo;
    private  CourseService courseService;
    private UserService userService;
    private NullPropertis nullPropertis;
    @Autowired
    public CourseController(CourseService courseService, NullPropertis nullPropertis, UserService userService, VideoService videoService, VideoRepo videoRepo) {
        this.courseService = courseService;
        this.nullPropertis = nullPropertis;
        this.userService = userService;
        this.videoService = videoService;
        this.videoRepo = videoRepo;
    }

    @PutMapping(value = "/updateCourse/{id}", consumes = "multipart/form-data")
    public void updateCourse(
            @RequestPart(value = "courseName") String courseName,
            @RequestPart(value = "title") String title,
            @RequestPart(value = "description") String description,
            @RequestPart(value = "category") String category,
            @RequestPart(value = "price") String price,
            @RequestPart(value = "difficultyLevel") String difficultyLevel,
            @PathVariable Long id
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getCourseById(id);
        if (courseService.isCourseOwnedByTeacher(user.getId(),id)){
            course.setDifficultyLevel(difficultyLevel);
            course.setPrice(Double.parseDouble(price));
            course.setTitle(title);
            course.setDescription(description);
            course.setCourseName(courseName);
            course.setCategory(category);
            courseService.sava(course);
        }

    }
    @PutMapping(value = "/updateCourseThumbnail/{id}", consumes = "multipart/form-data")
    public void updateCourseThumbnail(
            @RequestPart(value ="file") MultipartFile file,
            @PathVariable Long id
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getCourseById(id);
        if (courseService.isCourseOwnedByTeacher(user.getId(),id)){
           videoService.updateThumbnail(file,course);
        }
    }

    @Transactional
    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        videoRepo.deleteByCourseCourseId(courseId);
        courseService.deleteCourseById(courseId);
        return ResponseEntity.ok("Course and videos deleted");
    }
}
