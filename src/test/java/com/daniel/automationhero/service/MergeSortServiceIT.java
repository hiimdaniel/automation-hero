package com.daniel.automationhero.service;

import com.daniel.automationhero.AutomationHeroApplication;
import com.daniel.automationhero.config.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AutomationHeroApplication.class)
@ActiveProfiles("it")
public class MergeSortServiceIT {

    private static final String EXPECTED_OUTPUT_FILE_PATH = "src/test/resources/expected_output_file.txt";

    @Autowired
    private MergeSortService mergeSortService;

    @Autowired
    private FileIoService fileIoService;

    @Autowired
    private ResourceConfig resourceConfig;

    @BeforeEach
    public void setUp() {
        fileIoService.deleteFile(Path.of(resourceConfig.getOutputFilePath()));
    }

    @Test
    public void mergeSortShouldCreateNewSortedFile() {
        //GIVEN
        List<String> expectedOutputFileLines = fileIoService.getFileLinesAsStream(Path.of(EXPECTED_OUTPUT_FILE_PATH))
                .collect(Collectors.toList());

        //WHEN
        mergeSortService.externalMergeSortFile();

        //THEN
        List<String> outputFileLines = fileIoService.getFileLinesAsStream(Path.of(resourceConfig.getOutputFilePath()))
                .collect(Collectors.toList());

        assertEquals(expectedOutputFileLines.size(), outputFileLines.size());
        for (int i = 0; i < expectedOutputFileLines.size(); i++) {
            assertEquals(expectedOutputFileLines.get(i), outputFileLines.get(i));
        }

        fileIoService.deleteFile(Path.of(resourceConfig.getOutputFilePath()));
    }
}
