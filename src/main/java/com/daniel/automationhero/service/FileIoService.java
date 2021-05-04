package com.daniel.automationhero.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;

@Service
@Slf4j
public class FileIoService {

    public void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("An exception occurred during file deletion: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            log.warn("An exception occurred during file creation: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createFolderIfNotExist(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            log.warn("An exception occurred during folder creation: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void copyFileTo(Path from, Path to) {
        try {
            Files.copy(from, to, REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("An exception occurred during file copy: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Stream<String> getFileLinesAsStream(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            log.warn("An exception occurred during reading file as stream: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void writeNewLineToFile(Path path, String value) {
        String line = value + "\n";
        try {
            Files.write(path, line.getBytes(), APPEND);
        } catch (IOException e) {
            log.error("An exception occurred during writing to file. File path: {} Original exception message: {}", path.toString(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void writeListToFile(Path path, List<String> lines) {
        try {
            recreateFile(path);
            Files.write(path, lines);
        } catch (IOException e) {
            log.error("An exception occurred during writing to file. File path: {} Original exception message: {}", path, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void recreateFile(Path path) {
        deleteFile(path);
        createFile(path);
    }

    public void deleteAllFromFolder(Path path) {
        if (Files.isDirectory(path)) {
            getFilePathsInFolder(path).forEach(this::deleteFile);
        } else {
            throw new RuntimeException("The given path is not a folder!");
        }
    }

    public List<Path> getFilePathsInFolder(Path folder) {
        if (Files.isDirectory(folder)) {
            try {
                return Files
                        .walk(folder)
                        .filter(path -> !folder.equals(path))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                log.error("An exception occurred during getting file names from directory. Directory path: {} Original exception message: {}", folder, e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("The given path is not a folder!");
        }
    }
}
