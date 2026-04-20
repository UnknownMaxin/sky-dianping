package com.maxin;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@MapperScan("com.maxin.mapper")
@Slf4j
public class MaxinDPApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaxinDPApplication.class, args);
        log.info("Server started");
    }
}
