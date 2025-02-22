package imgehandelingbackend.imgehandelingbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


import imgehandelingbackend.imgehandelingbackend.model.ImageInfo;
import imgehandelingbackend.imgehandelingbackend.repo.ImageInfoRepository;
import imgehandelingbackend.imgehandelingbackend.service.ImageUploadingService;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ImageController {

    @Autowired
    private ImageUploadingService imageService;

    @Value("${file.image.upload-dir}") // Inject the upload directory from application.properties
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<ImageInfo> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description) throws IOException {

        // Ensure the upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save the file to the upload directory
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Create an ImageInfo object and save it to MongoDB
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setTitle(title);
        imageInfo.setDescription(description);
        imageInfo.setImagePath(filePath.toString());

        // Generate and set image hash
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        String imageHash = imageService.generateImageHash(bufferedImage);
        imageInfo.setImageHash(imageHash);

        ImageInfo savedImageInfo = imageService.saveImageInfo(imageInfo);

        return new ResponseEntity<>(savedImageInfo, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ImageInfo>> getAllImages() {
        List<ImageInfo> images = imageService.getAllImages();
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

    @PostMapping("/search")
    public ResponseEntity<List<ImageInfo>> searchSimilarImages(
            @RequestParam("file") MultipartFile file) throws IOException, NoSuchAlgorithmException {
        
        // Convert uploaded image to hash
        BufferedImage searchImage = ImageIO.read(file.getInputStream());
        String searchImageHash = imageService.generateImageHash(searchImage);

        // Get all images and compare hashes
        List<ImageInfo> allImages = imageService.getAllImages();
        List<ImageInfo> similarImages = new ArrayList<>();

        for (ImageInfo storedImage : allImages) {
            // Calculate similarity between hashes
            double similarity = imageService.calculateHashSimilarity(
                searchImageHash, 
                storedImage.getImageHash()
            );
            
            // Add images that are similar enough (e.g., >80% similar)
            if (similarity > 0.8) {
                similarImages.add(storedImage);
            }
        }

        return new ResponseEntity<>(similarImages, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ImageInfo>> searchImages(@RequestParam String query) {
        List<ImageInfo> allImages = imageService.getAllImages();
        List<ImageInfo> searchResults = allImages.stream()
                .filter(image -> 
                    image.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    image.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(searchResults, HttpStatus.OK);
    }

    // ... existing code ...

}
