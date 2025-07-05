package com.example.SmartLearn.Repository;

import com.example.SmartLearn.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepo extends JpaRepository<Teacher, Long> {
        void deleteByTeacherId(Long id);
}
