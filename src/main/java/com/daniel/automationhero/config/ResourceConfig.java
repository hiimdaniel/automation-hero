package com.daniel.automationhero.config;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@Builder
@ConfigurationProperties("automation-hero.resources")
public class ResourceConfig {

    private String inputFilePath;

    private String tempFilePath;

    private String outputFilePath;

    private String tempFolderPath;
}
