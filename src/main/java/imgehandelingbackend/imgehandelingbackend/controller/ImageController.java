package imgehandelingbackend.imgehandelingbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import imgehandelingbackend.imgehandelingbackend.model.ImageInfo;

import imgehandelingbackend.imgehandelingbackend.service.ImageUploadingService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://images.chandansharmablogs.tech/")
public class ImageController {

    @Autowired
    private ImageUploadingService imageService;

    @Value("${file.image.upload-dir}") // Inject the upload directory from application.properties
    private String uploadDir;

    @PostMapping("/uploadMultiple")
    public ResponseEntity<?> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("titles") List<String> titles,
            @RequestParam("descriptions") List<String> descriptions) throws IOException {

        if (files.length > 200) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Maximum 200 images can be uploaded at once.");
        }

        // Ensure the upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<ImageInfo> savedImages = new ArrayList<>();

        // Iterate over each file
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String title = titles.size() > i ? titles.get(i) : "Untitled";
            String description = descriptions.size() > i ? descriptions.get(i) : "";

            // Save the file to the upload directory
            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create an ImageInfo object and set details
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setTitle(title);
            imageInfo.setDescription(description);
            imageInfo.setImagePath(filePath.toString());

            // Generate and set image hash
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            String imageHash = imageService.generateImageHash(bufferedImage);
            imageInfo.setImageHash(imageHash);

            // Save to database
            ImageInfo savedImageInfo = imageService.saveImageInfo(imageInfo);
            savedImages.add(savedImageInfo);
        }

        return new ResponseEntity<>(savedImages, HttpStatus.CREATED);
    }

    @GetMapping("/images")
    public ResponseEntity<Page<ImageInfo>> getImages(@RequestParam(defaultValue = "0") int page) {
        Page<ImageInfo> images = imageService.getImages(page);
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) throws IOException {
        ImageInfo imageInfo = imageService.getImageInfoById(id);
        if (imageInfo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Path imagePath = Paths.get(imageInfo.getImagePath());
        byte[] imageBytes = Files.readAllBytes(imagePath);

        // Determine the media type based on file extension
        String fileName = imagePath.getFileName().toString().toLowerCase();
        MediaType mediaType;

        // Comprehensive mapping of file extensions to MIME types
        Map<String, String> mimeTypes = new HashMap<>();
        // Common web formats
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("webp", "image/webp");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("ico", "image/x-icon");
        // Raw formats
        mimeTypes.put("raw", "image/x-raw");
        mimeTypes.put("cr2", "image/x-canon-cr2");
        mimeTypes.put("nef", "image/x-nikon-nef");
        mimeTypes.put("arw", "image/x-sony-arw");
        // Professional formats
        mimeTypes.put("tiff", "image/tiff");
        mimeTypes.put("tif", "image/tiff");
        mimeTypes.put("bmp", "image/bmp");
        mimeTypes.put("psd", "image/vnd.adobe.photoshop");
        mimeTypes.put("ai", "application/pdf");
        mimeTypes.put("eps", "application/postscript");
        // Modern formats
        mimeTypes.put("heic", "image/heic");
        mimeTypes.put("heif", "image/heif");
        mimeTypes.put("avif", "image/avif");
        // Other formats
        mimeTypes.put("jxr", "image/vnd.ms-photo");
        mimeTypes.put("wdp", "image/vnd.ms-photo");
        mimeTypes.put("jpe", "image/jpeg");
        mimeTypes.put("jfif", "image/jpeg");
        mimeTypes.put("pcx", "image/x-pcx");
        mimeTypes.put("tga", "image/x-tga");
        mimeTypes.put("dds", "image/vnd.ms-dds");
        mimeTypes.put("xcf", "image/x-xcf");
        mimeTypes.put("pgm", "image/x-portable-graymap");
        mimeTypes.put("pbm", "image/x-portable-bitmap");
        mimeTypes.put("ppm", "image/x-portable-pixmap");
        mimeTypes.put("pnm", "image/x-portable-anymap");

        // Extract file extension
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        // Get MIME type from map, default to "application/octet-stream" if unknown
        String mimeType = mimeTypes.getOrDefault(extension, "application/octet-stream");
        mediaType = MediaType.parseMediaType(mimeType);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageBytes);
    }



    @GetMapping("/search")
    public ResponseEntity<List<ImageInfo>> searchImages(@RequestParam String query) {
        List<ImageInfo> searchResults = imageService.searchImages(query);
        return new ResponseEntity<>(searchResults, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String id) {
        try {
            // Get image info before deletion
            ImageInfo imageInfo = imageService.getImageInfoById(id);
            if (imageInfo == null) {
                return new ResponseEntity<>(
                        Map.of("message", "Image not found"),
                        HttpStatus.NOT_FOUND);
            }

            // Delete file from filesystem
            Path imagePath = Paths.get(imageInfo.getImagePath());
            Files.deleteIfExists(imagePath);

            // Delete from database
            imageService.deleteImageInfo(id);

            return new ResponseEntity<>(
                    Map.of("message", "Image deleted successfully"),
                    HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(
                    Map.of("message", "Error deleting image: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
