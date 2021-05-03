package com.daniel.automationhero;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;

@Service
@Slf4j
public class FileSorting {

    @Value("${automation-hero.resources.input-file-path}")
    private String inputFilePath;

    @Value("${automation-hero.resources.output-file-path}")
    private String outputFilePath;

    @Value("${automation-hero.resources.temp-file-path}")
    private String tempFilePath;

    public void bubbleSortFile() throws IOException {
        log.info("Processing input file at {}", inputFilePath);
        long start = System.nanoTime();
        Files.deleteIfExists(Path.of(tempFilePath));
        Files.deleteIfExists(Path.of(outputFilePath));
        Files.createFile(Path.of(tempFilePath));
        Files.copy(Path.of(inputFilePath), Path.of(tempFilePath), REPLACE_EXISTING);
        bubbleSortFileRecursively(tempFilePath, outputFilePath, 1L);
        Files.deleteIfExists(Path.of(tempFilePath));
        log.info("Processing finished in seconds: {}", (System.nanoTime() - start) / 1000000000);
    }

    private void bubbleSortFileRecursively(String inputFilePathString, String outputFilePathString, Long turn) throws IOException {
        AtomicReference<Boolean> swapped = new AtomicReference<>(false);
        AtomicReference<Integer> prevItemHolder = new AtomicReference<>(null);
        AtomicInteger numberOfSwaps = new AtomicInteger(0);
        Files.deleteIfExists(Path.of(outputFilePathString));
        Files.createFile(Path.of(outputFilePathString));
        Stream<String> lines = Files.lines(Path.of(inputFilePathString));
        lines.forEachOrdered(actLine -> {
            Integer actItem = Integer.parseInt(actLine);
            if (prevItemHolder.get() != null) {
                Integer prevItem = prevItemHolder.get();
                if (prevItem >= actItem) {
                    writeNewLineToFile(outputFilePathString, actItem);
                    swapped.set(true);
                } else {
                    writeNewLineToFile(outputFilePathString, prevItem);
                    prevItemHolder.set(actItem);
                }
                numberOfSwaps.incrementAndGet();
            } else {
                prevItemHolder.set(actItem);
            }
        });
        writeNewLineToFile(outputFilePathString, prevItemHolder.get());
        log.info("Iterated through file for sorting. Round: {} Number of swaps in this turn: {}", turn, numberOfSwaps.get());
        if (swapped.get()) {
            bubbleSortFileRecursively(outputFilePathString, inputFilePathString, turn + 1);
        }
    }

    private void writeNewLineToFile(String pathToFile, Integer value) {
        String line = value + "\n";
        try {
            Files.write(Path.of(pathToFile), line.getBytes(), APPEND);
        } catch (IOException e) {
            log.error("An exception occurred during writing to file. File path: {} Original exception message: {}", pathToFile, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
