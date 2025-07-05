package com.example.SmartLearn.Controller;

import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Entity.Video;
import com.example.SmartLearn.Service.CourseService;
import com.example.SmartLearn.Service.UserService;
import com.example.SmartLearn.Service.VideoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Video")
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private UserService userService;
    @Autowired
    private CourseService courseService;


    @GetMapping("/Cid/{Cid}")
    public ResponseEntity<List<Video>> getAllVideos(Long Cid){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Course course = courseService.getCourseById(Cid);
        if (course.getStudents().contains(user.getStudent())) {
            return new ResponseEntity<>(course.getVideos(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GetMapping("/{id}")
    public Video getById(@PathVariable  Long id){
        return videoService.getVideoById(id);
    }


}
