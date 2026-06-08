package com.flashgo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.flashgo")
public class FlashGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashGoApplication.class, args);
    }
}
