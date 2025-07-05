package com.example.SmartLearn.Controller;

import com.example.SmartLearn.DTO.CourseDTO;
import com.example.SmartLearn.DTO.UserDTO;
import com.example.SmartLearn.DTO.VideoDTO;
import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Entity.Video;
import com.example.SmartLearn.Repository.CourseRepo;
import com.example.SmartLearn.Repository.TeacherRepo;
import com.example.SmartLearn.Service.*;
import com.example.SmartLearn.util.NullPropertis;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/Instructor")
public class InstructorController {

    private final VideoService videoService;
    private final NullPropertis nullPropertis;
    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    private TeacherRepo teacherRepo;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    public InstructorController(NullPropertis nullPropertis, CourseService courseService, UserService userService, VideoService videoService) {
        this.nullPropertis = nullPropertis;
        this.courseService = courseService;
        this.userService = userService;
        this.videoService = videoService;
    }

    @DeleteMapping("/Delete")
    public ResponseEntity<String> deleteInstructor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if (user != null && user.getTeacher() != null) {
            teacherRepo.deleteByTeacherId(user.getTeacher().getTeacherId());
            userService.deleteUser(user.getUsername());
            return ResponseEntity.ok("Instructor and associated user deleted successfully.");
        }
        return ResponseEntity.badRequest().body("Failed to delete instructor.");
    }

    @GetMapping("/Details")
    public ResponseEntity<User> getInstructorById() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(404).build();
    }

    @PutMapping("/Update")
    public ResponseEntity<String> updateInstructor(@RequestBody UserDTO userDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.findByUsername(auth.getName());
        if (existingUser != null) {
            BeanUtils.copyProperties(userDTO, existingUser, nullPropertis.getNullProperty(userDTO));
            if (userDTO.getPassword() == null) {
                userService.save(existingUser);
            } else {
                userService.update(existingUser);
            }
            return ResponseEntity.ok("User updated successfully.");
        }
        return ResponseEntity.badRequest().body("User update failed.");
    }


    @PostMapping("/New-Course")
    public ResponseEntity<String> addCourse(    @RequestPart("title") String title,
                                                @RequestParam("courseName")String courseName,
                                                @RequestPart("difficultyLevel") String difficultyLevel,
                                                @RequestPart("price") String price,
                                                @RequestPart("file") MultipartFile file

    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if (user != null && user.getTeacher() != null) {
            Course course = new Course();
            course.setCourseName(courseName);
            course.setDifficultyLevel(difficultyLevel);
            course.setPrice(Float.parseFloat(price));
            course.setTitle(title);
            course.setTeacherName(user.getName());
            course.setInstructor(user.getTeacher());
            videoService.uploadThumbnail(file, course);
            return ResponseEntity.ok("Course added successfully.");
        }
        return ResponseEntity.badRequest().body("Course creation failed.");
    }



    @GetMapping("/My-Courses")
    public ResponseEntity<List<CourseDTO>> getMyCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if (user != null && user.getTeacher() != null) {
            return new ResponseEntity<>(courseRepo.findOwnedCoursesByUserIdLite(user.getId()), HttpStatus.OK);
        }
        return ResponseEntity.status(404).build();
    }

    @PutMapping(value = "/UpdateVideoDetails/{Vid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @PathVariable Long Vid ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getByVideoId(Vid);
        if (courseService.isCourseOwnedByTeacher(user.getId(),course.getCourseId())){
            videoService.updateVideoDetails(title,description,Vid);
            return ResponseEntity.ok("Video details updated successfully.");
        }
        return ResponseEntity.status(403).body("Unauthorized to update this video.");
    }

    @PutMapping(value = "/UpdateVideoFile/{Vid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateVideoFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long Vid ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getByVideoId(Vid);
        if (courseService.isCourseOwnedByTeacher(user.getId(),course.getCourseId())){
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(400).body("File is empty.");
            }
            videoService.updateVideo(file,Vid);
            return ResponseEntity.ok("Video File updated successfully.");
        }
        return ResponseEntity.status(403).body("Unauthorized to update this video.");
    }


    @DeleteMapping("/DeleteVideoFromCourses/{Vid}")
    public ResponseEntity<String> deleteVideoFromCourses(@PathVariable Long Vid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getByVideoId(Vid);
        Video video = videoService.getVideoById(Vid);
        if (user.getTeacher().getCourses().contains(course)) {
            course.setDuration(course.getDuration() - video.getDuration());
            course.getVideos().remove(video);
            courseService.sava(course);
            videoService.deleteVideo(Vid);
            return ResponseEntity.ok("Video deleted from course.");
        }
        return ResponseEntity.status(403).body("Unauthorized to delete this video.");
    }

    @PostMapping("/addVideo")
    public ResponseEntity<String> addVideo(@RequestParam("title") String title,
                                           @RequestParam("courseId") Long courseId,
                                           @RequestParam("description") String description,
                                           @RequestParam("file") MultipartFile video) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long id = userService.findByUsername(auth.getName()).getId();
        if (courseService.isCourseOwnedByTeacher(id, courseId)) {
            if (videoService.createVideo(video, title, description, courseId)) {
                return ResponseEntity.ok("Video uploaded successfully.");
            }
            return ResponseEntity.status(500).body("Failed to upload video. Please try again.");
        }
        return ResponseEntity.status(403).body("Unauthorized to upload video.");
    }
}
