package com.example.SmartLearn.Repository;

import com.example.SmartLearn.DTO.UserDTO;
import com.example.SmartLearn.Entity.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepo extends JpaRepository<User, Long> {
     User findByUsername(String username);
     User findByUsernameOrEmail(String username, String email);
    @Query("SELECT new com.example.SmartLearn.DTO.UserDTO(u.username, u.name, u.gender, u.email, u.phone, u.address) " +
            "FROM User u WHERE u.username = :username OR u.email = :email")
    UserDTO findLiteUserDetail(String username, String email);
     List<User> findByRole(String role);
    boolean deleteByUsername(String username);
    void deleteByEmail(String email);
    User findByEmail(@Email(message = "Invalid email format") String email);
}

