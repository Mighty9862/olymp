package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }

            // Проверяем права на запись
            File testFile = new File(uploadDir + "/test.txt");
            testFile.createNewFile();
            testFile.delete();

            System.out.println("Upload directory is writable: " + uploadDir);
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            if (!file.getContentType().startsWith("image/")) {
                throw new IOException("File is not an image: " + file.getOriginalFilename());
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path path = Paths.get(uploadDir + File.separator + fileName);

            System.out.println("Saving file to: " + path.toAbsolutePath());

            Files.copy(file.getInputStream(), path);
            urls.add("/uploads/" + fileName);
        }
        return urls;
    }

    public List<String> getAllImageUrls() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(uploadPath)) {
            return paths.filter(Files::isRegularFile)
                    .map(p -> "/uploads/" + p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    public void deleteImage(String fileName) throws IOException {
        try {
            // Извлекаем только имя файла из полного пути (если передан полный путь)
            String cleanFileName = extractFileNameFromPath(fileName);

            Path path = Paths.get(uploadDir + File.separator + cleanFileName);

            System.out.println("Attempting to delete file: " + path.toAbsolutePath());

            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted successfully: " + cleanFileName);
            } else {
                System.err.println("File not found: " + path.toAbsolutePath());
                throw new IOException("Image not found: " + cleanFileName);
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
            throw new IOException("Error deleting image: " + e.getMessage(), e);
        }
    }

    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }

        // Если передан полный путь типа "/uploads/filename.jpg", извлекаем только имя файла
        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        }

        // Если передан только filename, возвращаем как есть
        return filePath;
    }
}