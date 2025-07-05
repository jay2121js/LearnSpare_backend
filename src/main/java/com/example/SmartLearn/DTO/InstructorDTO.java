package com.example.SmartLearn.DTO;

import com.example.SmartLearn.Entity.Course;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class InstructorDTO {
    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Password is required")
    private String password;

    private String name;

    @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
    private String phone;

    private String address;
    @Email(message = "invalid Email")

    private String email;

}
