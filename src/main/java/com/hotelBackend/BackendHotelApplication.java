package com.hotelBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling // Para que el sheduler de NO_PRESENTADA pueda ejecutarse
public class BackendHotelApplication {


    public static void main(String[] args) {

        SpringApplication.run(BackendHotelApplication.class, args);
    }

}
