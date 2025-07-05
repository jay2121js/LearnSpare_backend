package com.example.SmartLearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SmartLearnAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLearnAppApplication.class, args);
    }

}
