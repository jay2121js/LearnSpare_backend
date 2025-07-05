package com.example.SmartLearn.Controller;

import com.example.SmartLearn.DTO.UserDTO;
import com.example.SmartLearn.Entity.User;
import com.example.SmartLearn.Repository.UserRepo;
import com.example.SmartLearn.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/User")
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @GetMapping("/userdata")
    public ResponseEntity<UserDTO> getUserdata() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO user = userRepo.findLiteUserDetail(auth.getName(),auth.getName());
        return ResponseEntity.ok(user);
    }
    @PutMapping(value = "/profileupdate", consumes = "multipart/form-data")
    public void  updateProfile(
            @RequestParam("username") String  username,
            @RequestParam("fullName") String  fullName,
            @RequestParam("gender") String  gender,
            @RequestParam("phone") String  phone,
            @RequestParam("address") String  address,
            @RequestParam("email") String email
    ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        user.setName(fullName);
        user.setGender(gender);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUsername(username);
        userService.update(user);
    }

    @PutMapping(value = "/password", consumes = "multipart/form-data")
    public void updatePassword(
            @RequestParam("password") String password){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        user.setPassword(password);
        userService.update(user);
    }
}
