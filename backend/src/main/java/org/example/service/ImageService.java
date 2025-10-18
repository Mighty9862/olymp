package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    private Path orderFile;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }

            // Файл для хранения порядка изображений
            orderFile = uploadPath.resolve("image_order.json");
            
            // Если файл порядка не существует, создаем пустой список
            if (!Files.exists(orderFile)) {
                saveImageOrder(new ArrayList<>());
            }

            System.out.println("Upload directory is writable: " + uploadDir);
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        List<String> currentOrder = loadImageOrder();
        
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
            String imageUrl = "/uploads/" + fileName;
            urls.add(imageUrl);
            
            // Добавляем новое изображение в конец списка порядка
            currentOrder.add(imageUrl);
        }
        
        // Сохраняем обновленный порядок
        saveImageOrder(currentOrder);
        
        return urls;
    }

    public List<String> getAllImageUrls() throws IOException {
        List<String> order = loadImageOrder();
        
        // Фильтруем только существующие файлы
        List<String> existingImages = order.stream()
                .filter(url -> {
                    String fileName = extractFileNameFromPath(url);
                    Path filePath = Paths.get(uploadDir + File.separator + fileName);
                    return Files.exists(filePath);
                })
                .collect(Collectors.toList());
        
        // Если порядок изменился (некоторые файлы были удалены вручную), сохраняем исправленный порядок
        if (existingImages.size() != order.size()) {
            saveImageOrder(existingImages);
        }
        
        return existingImages;
    }

    public void deleteImage(String fileName) throws IOException {
        try {
            String cleanFileName = extractFileNameFromPath(fileName);
            Path path = Paths.get(uploadDir + File.separator + cleanFileName);

            System.out.println("Attempting to delete file: " + path.toAbsolutePath());

            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted successfully: " + cleanFileName);
                
                // Удаляем изображение из порядка
                List<String> currentOrder = loadImageOrder();
                String imageUrlToRemove = "/uploads/" + cleanFileName;
                currentOrder.remove(imageUrlToRemove);
                saveImageOrder(currentOrder);
                
            } else {
                System.err.println("File not found: " + path.toAbsolutePath());
                throw new IOException("Image not found: " + cleanFileName);
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
            throw new IOException("Error deleting image: " + e.getMessage(), e);
        }
    }

    public void updateImageOrder(List<String> newOrder) throws IOException {
        // Валидация: проверяем, что все файлы в новом порядке существуют
        for (String imageUrl : newOrder) {
            String fileName = extractFileNameFromPath(imageUrl);
            Path filePath = Paths.get(uploadDir + File.separator + fileName);
            if (!Files.exists(filePath)) {
                throw new IOException("Image not found: " + fileName);
            }
        }
        
        saveImageOrder(newOrder);
    }

    private List<String> loadImageOrder() throws IOException {
        if (!Files.exists(orderFile)) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(orderFile.toFile(), new TypeReference<List<String>>() {});
    }

    private void saveImageOrder(List<String> order) throws IOException {
        objectMapper.writeValue(orderFile.toFile(), order);
    }

    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }

        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        }

        return filePath;
    }
}