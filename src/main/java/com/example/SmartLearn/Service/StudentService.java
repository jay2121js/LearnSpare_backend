package com.example.SmartLearn.Service;

import com.example.SmartLearn.Entity.Student;
import com.example.SmartLearn.Repository.StudentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepo studentRepo;

    public List<Student> getAllStudent(){
        return studentRepo.findAll();
    }

}
