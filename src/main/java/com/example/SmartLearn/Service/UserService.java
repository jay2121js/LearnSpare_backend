package com.example.SmartLearn.Service;

import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.Student;
import com.example.SmartLearn.Entity.Teacher;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Enum.Role;
import com.example.SmartLearn.Repository.CourseRepo;
import com.example.SmartLearn.Repository.StudentRepo;
import com.example.SmartLearn.Repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final StudentRepo studentRepo;
    private CourseRepo courseRepo;
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(CourseRepo courseRepo, UserRepo userRepo, PasswordEncoder passwordEncoder, StudentRepo studentRepo) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.studentRepo = studentRepo;
    }



    public List<Course> getCourses(String username) {
        return userRepo.findByUsername(username).getStudent().getCourses();
    }

    public void save(User user){
       userRepo.save(user);
    }

    public void update(User user){
         user.setPassword(passwordEncoder.encode(user.getPassword()));
         userRepo.save(user);
    }
    @Transactional
    public void saveUser(User user, String role){
        User existingUser = findByUsername(user.getUsername());
        if (existingUser != null) {
            return;
        }
        if (role.equalsIgnoreCase("STUDENT")) {
            user.setRole(Role.STUDENT);
            Student student = new Student();
            student.setUser(user); // Associate user with student
            user.setStudent(student);
        } else if (role.equalsIgnoreCase("TEACHER")) {
            user.setRole(Role.TEACHER);
            Teacher teacher = new Teacher();
            teacher.setUser(user);
            user.setTeacher(teacher);

        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);
    }

    public User findByUsername(String username){
        User user = userRepo.findByUsername(username);
        if (user == null) {
            user = userRepo.findByEmail(username);
        }
        return user;
    }
    @Transactional
    public void EnrollCourse(String username, Long Cid){
        User user = findByUsername(username);
        Student student = user.getStudent();
        Course course = courseRepo.findById(Cid).orElse(null);
        if (course != null && student != null) {
            if (!courseRepo.isCourseEnrolledByUser(user.getId(), Cid)) {
                student.getCourses().add(course);
                course.setEnrolledUsers(course.getEnrolledUsers()+1);
                studentRepo.save(student);
                courseRepo.save(course);
            }
        }
    }


    @Transactional
    public void deleteUser(String username){
      userRepo.deleteByUsername(username);
    }


}
