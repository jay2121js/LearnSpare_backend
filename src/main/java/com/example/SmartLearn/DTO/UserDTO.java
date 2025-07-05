package com.example.SmartLearn.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Password is required")
    private String password;

    private String name;

    private String gender;

    public UserDTO(String username, String name, String gender, String email, String phone, String address) {
        this.username = username;
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
    private String phone;

    private String address;

    @NotNull(message = "Role is required")
    private String role;


}