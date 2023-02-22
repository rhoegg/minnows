package com.ryanhoegg.minnowserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class MinnowserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinnowserverApplication.class, args);
    }

}
