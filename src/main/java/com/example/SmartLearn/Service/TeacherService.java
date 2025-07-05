package com.example.SmartLearn.Service;

import com.example.SmartLearn.Entity.Course;
import com.example.SmartLearn.Entity.Student;
import com.example.SmartLearn.Entity.Teacher;
import com.example.SmartLearn.Repository.StudentRepo;
import com.example.SmartLearn.Repository.TeacherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    @Autowired
    private TeacherRepo teacherRepo;
    private CourseService courseService;
    public List<Teacher> getAllStudent(){
        return teacherRepo.findAll();
    }
    }
