package com.example.SmartLearn.Repository;

import com.example.SmartLearn.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {
    Optional<Student> findByUser_Username(String username);
}
