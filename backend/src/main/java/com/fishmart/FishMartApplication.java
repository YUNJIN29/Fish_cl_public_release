package com.fishmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FishMartApplication {
  public static void main(String[] args) {
    SpringApplication.run(FishMartApplication.class, args);
  }
}

