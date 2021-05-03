package com.daniel.automationhero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class AutomationHeroApplication {

    public static void main(String[] args) throws IOException {
        var ctx = SpringApplication.run(AutomationHeroApplication.class, args);
        FileSorting service = ctx.getBean(FileSorting.class);
        service.bubbleSortFile();
    }
}
