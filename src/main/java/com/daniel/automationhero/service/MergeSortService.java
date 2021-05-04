package com.daniel.automationhero.service;

import com.daniel.automationhero.config.ResourceConfig;
import com.daniel.automationhero.config.SortingConfig;
import com.daniel.automationhero.model.ChunkElement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class MergeSortService {

    private static final String TEMP_FILE_BASE_NAME = "temp_file_";

    private final FileIoService fileIoService;
    private final SortingConfig sortingConfig;
    private final ResourceConfig resourceConfig;


    public void externalMergeSortFile() {
        log.info("Processing input file at {}", resourceConfig.getInputFilePath());
        long start = System.nanoTime();
        fileIoService.createFolderIfNotExist(Path.of(resourceConfig.getTempFolderPath()));
        fileIoService.deleteFile(Path.of(resourceConfig.getOutputFilePath()));
        fileIoService.createFile(Path.of(resourceConfig.getOutputFilePath()));
        long tempInputFileCount = sliceInputFileToChunksAndWrite();
        log.info("Created {} temp files with size of {} under {} folder in {} seconds.", tempInputFileCount, sortingConfig.getChunkSize(), resourceConfig.getTempFolderPath(), (System.nanoTime() - start) / 1000000000);
        log.info("Start merging the temp files.");
        List<ChunkElement> chunks = readSortedChunks();
        mergeChunksAndWriteToFile(chunks);
        fileIoService.deleteAllFromFolder(Path.of(resourceConfig.getTempFolderPath()));
        log.info("Processing finished in seconds: {}", (System.nanoTime() - start) / 1000000000);
    }

    /**
     * Iterates through the input file and slices it to chunk size arrays. Sorting the arrays one by one and writing them to the temp folder as separated files.
     */
    private long sliceInputFileToChunksAndWrite() {
        List<String> chunk = new ArrayList<>();
        AtomicLong index = new AtomicLong(1L);
        AtomicLong postFix = new AtomicLong(1L);

        fileIoService.getFileLinesAsStream(Path.of(resourceConfig.getInputFilePath()))
                .forEachOrdered(line -> {
                    chunk.add(line);
                    if (index.get() % sortingConfig.getChunkSize() == 0) {
                        sortListAndWriteToFile(chunk, postFix.get());
                        postFix.incrementAndGet();
                        chunk.clear();
                    }
                    index.incrementAndGet();
                });
        //Write the last chunk to file if the number of lines in the file are not %chunksize==0
        if (!chunk.isEmpty()) {
            sortListAndWriteToFile(chunk, postFix.incrementAndGet());
        }
        return postFix.get();
    }

    /**
     * Sorts and writes a chunk to the temp folder. Using default List.sort which is a special merge sort algorithm with O(N) time complexity.
     *
     * @param lines   Chunk to be written.
     * @param postFix Postfix of the file, need to be unique (practically the index).
     */
    private void sortListAndWriteToFile(List<String> lines, Long postFix) {
        String pathString = resourceConfig.getTempFolderPath() + "/" + TEMP_FILE_BASE_NAME + postFix;
        lines.sort(String::compareTo);
        fileIoService.writeListToFile(Path.of(pathString), lines);
    }

    /**
     * Reads the chunk files in the temp output folder and initializes list of streams for further processing.
     *
     * @return List of Stream iterators with additional functionalities.
     */
    private List<ChunkElement> readSortedChunks() {
        List<ChunkElement> result = new ArrayList<>();
        List<Path> filePaths = fileIoService.getFilePathsInFolder(Path.of(resourceConfig.getTempFolderPath()));
        filePaths.forEach(path -> {
            Stream<String> fileLineStream = fileIoService.getFileLinesAsStream(path);
            ChunkElement chunkElement = new ChunkElement(fileLineStream.iterator());
            result.add(chunkElement);
        });
        return result;
    }

    /**
     * Merges the chunk files together and writing it to a file by finding the smallest element of the next value of every chunk file iterator.
     *
     * @param chunks List of Chunks to merge
     */
    private void mergeChunksAndWriteToFile(List<ChunkElement> chunks) {
        //Find min value of current element of chunk iterators and write it to a file.
        boolean chunksAreEmpty = false;

        while (!chunksAreEmpty) {
            ChunkElement minChunk = chunks.get(0);
            for (ChunkElement chunk : chunks) {
                if (chunk.readValue().isPresent() && minChunk.readValue().isPresent()) {
                    if (chunk.readValue().get().compareTo(minChunk.readValue().get()) < 0) {
                        minChunk = chunk;
                    }
                }
            }
            if (minChunk.readValue().isPresent()) {
                log.debug("Writing to output file: {}", minChunk.readValue().get());
                fileIoService.writeNewLineToFile(Path.of(resourceConfig.getOutputFilePath()), minChunk.getValueAndPollNew().get());
            } else {
                chunksAreEmpty = true;
            }
        }

    }


}
