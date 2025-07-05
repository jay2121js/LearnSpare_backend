package com.example.SmartLearn.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentDTO {

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Password is required")
    private String password;

    private String name;

    private String gender;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
    private String phone;

    private String address;


}
