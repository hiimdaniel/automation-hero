package com.daniel.automationhero.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties("automation-hero.sorting.merge")
public class SortingConfig {

    private Long chunkSize;
}
