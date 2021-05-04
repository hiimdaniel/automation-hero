package com.daniel.automationhero;

import com.daniel.automationhero.service.MergeSortService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class AutomationHeroApplication {

    public static void main(String[] args) throws IOException {
        var ctx = SpringApplication.run(AutomationHeroApplication.class, args);
        MergeSortService service = ctx.getBean(MergeSortService.class);
        service.externalMergeSortFile();
    }
}
