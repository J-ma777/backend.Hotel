package com.hotelBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendHotelApplication {


    public static void main(String[] args) {

        SpringApplication.run(BackendHotelApplication.class, args);
    }

}
