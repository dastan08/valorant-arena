package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FileUploadController {
    
    private static final String TEAM_LOGO_DIR = "uploads/team-logos/";
    private static final String TOURNAMENT_BANNER_DIR = "uploads/tournament-banners/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    @PostMapping("/team-logo")
    public ResponseEntity<ApiResponse<String>> uploadTeamLogo(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
            }
            
            // Check file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size must be less than 5MB"));
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only image files are allowed"));
            }
            
            // Create upload directory if it doesn't exist
            File uploadDir = new File(TEAM_LOGO_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = Paths.get(TEAM_LOGO_DIR + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the URL path
            String fileUrl = "/uploads/team-logos/" + filename;
            
            log.info("Team logo uploaded successfully: {}", fileUrl);
            
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
            
        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
    
    @PostMapping("/tournament-banner")
    public ResponseEntity<ApiResponse<String>> uploadTournamentBanner(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
            }
            
            // Check file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size must be less than 5MB"));
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only image files are allowed"));
            }
            
            // Create upload directory if it doesn't exist
            File uploadDir = new File(TOURNAMENT_BANNER_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = Paths.get(TOURNAMENT_BANNER_DIR + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the URL path
            String fileUrl = "/uploads/tournament-banners/" + filename;
            
            log.info("Tournament banner uploaded successfully: {}", fileUrl);
            
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
            
        } catch (IOException e) {
            log.error("Error uploading tournament banner", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
}
