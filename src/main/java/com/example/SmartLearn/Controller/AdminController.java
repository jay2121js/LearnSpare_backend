package com.example.SmartLearn.Controller;

import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.Student;
import com.example.SmartLearn.Entity.Teacher;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Service.CourseService;
import com.example.SmartLearn.Service.StudentService;
import com.example.SmartLearn.Service.TeacherService;
import com.example.SmartLearn.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/Admin")
public class AdminController {

        private StudentService studentService;
        private UserService userService;
        private CourseService courseService;
        private TeacherService teacherService;

        @Autowired
        public AdminController(StudentService studentService, UserService userService, CourseService courseService, TeacherService teacherService) {
            this.studentService = studentService;
            this.userService = userService;
            this.courseService = courseService;
            this.teacherService = teacherService;
        }

        @GetMapping("/username/{username}")
        public User getUserByUsername(@PathVariable String username) {
            return userService.findByUsername(username);
        }

        @DeleteMapping("/username/{username}")
        public void deleteUserByUsername(@PathVariable String username) {
         userService.deleteUser(username);
        }

        @GetMapping("All-Courses")
        public List<Course> getAllCourses() {
            return courseService.getAllCourses();
        }

        @GetMapping("/Students")
        public List<Student> getAllStudents() {
        return studentService.getAllStudent();
        }

        @GetMapping("/Teacher")
        public List<Teacher> getAllTeachers() {
            return teacherService.getAllStudent();
        }
}
