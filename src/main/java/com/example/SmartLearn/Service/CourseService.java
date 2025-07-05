package com.example.SmartLearn.Service;

import com.example.SmartLearn.DTO.CourseDTO;
import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Repository.CourseRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private CourseRepo courseRepo;
    @Autowired

    public CourseService(CourseRepo courseRepo) {
        this.courseRepo = courseRepo;
    }
    public void addCourse(Course course) {
        courseRepo.save(course);
    }
    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }
    public List<Course> getCourseByName(String courseName) {
        return courseRepo.findByCourseNameContaining(courseName);
    }
    @Transactional
    public Course getCourseById(Long courseId) {
        return  courseRepo.findCourseWithVideosById(courseId);
    }
    public void sava(Course course) {
        courseRepo.save(course);
    }
    @Transactional
    public void deleteCourseById(Long courseId) {
        courseRepo.deleteStudentCourseAssociations(courseId);
        courseRepo.hardDeleteById(courseId);
    }
    public List<CourseDTO> getLiteCourse() {
        long start = System.currentTimeMillis();
        List<CourseDTO> result = courseRepo.fetchCourseSummaries();
        long end = System.currentTimeMillis();
        System.out.println("DB Fetch Time: " + (end - start) + " ms");
        return result;
    }

    public Boolean isCourseOwnedByTeacher(Long teacherId, Long courseId) {
       return courseRepo.isCourseOwnedByUser(teacherId, courseId);

    }
    public Course getByVideoId(Long videoId) {
       return courseRepo.getCourseByVideosId(videoId);
    }

    public boolean isUserEnrolledInCourse(Long userId, Long courseId) {
        return courseRepo.isCourseEnrolledByUser(userId, courseId);
    }

    public List<Long> getEnrolledCourseIdsForCurrentUser(Long userId) {
        return courseRepo.findEnrolledCourseIdsByUser(userId);
    }
    public List<Long> getOwnedCourseIdsForCurrentUser(Long userId) {
        return courseRepo.findOwnedCourseIdsByUser(userId);
    }


    public List<Long> getEnrolledCourseIdsByUserId(Long userId) {
        return courseRepo.findEnrolledCourseIdsByUser(userId);
    }
}
