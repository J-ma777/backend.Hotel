package com.hotelbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Para que el sheduler de NO_PRESENTADA pueda ejecutarse
public class BackendHotelApplication {


    public static void main(String[] args) {

        SpringApplication.run(BackendHotelApplication.class, args);
    }

}
