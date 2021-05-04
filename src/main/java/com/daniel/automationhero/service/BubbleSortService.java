package com.daniel.automationhero.service;

import com.daniel.automationhero.config.ResourceConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@AllArgsConstructor
public class BubbleSortService {

    private final FileIoService fileIoService;

    private final ResourceConfig resourceConfig;

    public void externalBubbleSortFile() {
        log.info("Processing input file at {}", resourceConfig.getInputFilePath());
        long start = System.nanoTime();
        fileIoService.deleteFile(Path.of(resourceConfig.getOutputFilePath()));
        fileIoService.recreateFile(Path.of(resourceConfig.getTempFilePath()));
        fileIoService.copyFileTo(Path.of(resourceConfig.getInputFilePath()), Path.of(resourceConfig.getTempFilePath()));
        bubbleSortFileRecursively(resourceConfig.getTempFilePath(), resourceConfig.getOutputFilePath(), 1L);
        fileIoService.deleteFile(Path.of(resourceConfig.getTempFilePath()));
        log.info("Processing finished in seconds: {}", (System.nanoTime() - start) / 1000000000);
    }

    /**
     * Bubble sort the content of a file using a temp file recursively. Time complexity is ~ O(n^2)
     *
     * @param tempFilePathString   Path to the temp file. Initially the temp file has to contain every element of the input file.
     * @param outputFilePathString Path to the output file.
     * @param turn                 Used for logging during recursive calls, should be 1 for init.
     */
    private void bubbleSortFileRecursively(String tempFilePathString, String outputFilePathString, Long turn) {
        AtomicReference<Boolean> swapped = new AtomicReference<>(false);
        AtomicReference<Integer> prevItemHolder = new AtomicReference<>(null);
        AtomicInteger numberOfSwaps = new AtomicInteger(0);
        fileIoService.recreateFile(Path.of(outputFilePathString));
        fileIoService.getFileLinesAsStream(Path.of(tempFilePathString)).forEachOrdered(actLine -> {
            Integer actItem = Integer.parseInt(actLine);
            if (prevItemHolder.get() != null) {
                Integer prevItem = prevItemHolder.get();
                if (prevItem >= actItem) {
                    fileIoService.writeNewLineToFile(Path.of(outputFilePathString), actItem.toString());
                    swapped.set(true);
                } else {
                    fileIoService.writeNewLineToFile(Path.of(outputFilePathString), prevItem.toString());
                    prevItemHolder.set(actItem);
                }
                numberOfSwaps.incrementAndGet();
            } else {
                prevItemHolder.set(actItem);
            }
        });
        fileIoService.writeNewLineToFile(Path.of(outputFilePathString), prevItemHolder.get().toString());
        log.debug("Iterated through file for sorting. Round: {} Number of swaps in this turn: {}", turn, numberOfSwaps.get());
        if (swapped.get()) {
            bubbleSortFileRecursively(outputFilePathString, tempFilePathString, turn + 1);
        }
    }
}
