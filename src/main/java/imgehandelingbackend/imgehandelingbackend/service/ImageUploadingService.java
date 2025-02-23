package imgehandelingbackend.imgehandelingbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import imgehandelingbackend.imgehandelingbackend.model.ImageInfo;
import imgehandelingbackend.imgehandelingbackend.repo.ImageInfoRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.IIOImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.io.*;


import javax.imageio.ImageIO;

@Service
public class ImageUploadingService {




    @Autowired
    private  ImageInfoRepository imageInfoRepository;

    public ImageInfo saveImageInfo(ImageInfo imageInfo) {
        return imageInfoRepository.save(imageInfo);
    }

    public Page<ImageInfo> getImages(int page) {
        Pageable pageable = PageRequest.of(page, 15); // Fetch 15 images per request
        return imageInfoRepository.findAll(pageable);
    }

    public ImageInfo getImageInfoById(String id) {
        return imageInfoRepository.findById(id).orElse(null);
    }

    public List<ImageInfo> searchImages(String query) {
        return imageInfoRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }


    //comenting the code for cloudinary image storing







    // private final Cloudinary cloudinary;
    // private final ImageInfoRepository imageInfoRepository;

    // @Autowired
    // public ImageUploadingService(Cloudinary cloudinary, ImageInfoRepository imageInfoRepository) {
    //     this.cloudinary = cloudinary;
    //     this.imageInfoRepository = imageInfoRepository;
    // }

    // public ImageInfo uploadingImage(MultipartFile file, String title, String description)
    //         throws IOException, NoSuchAlgorithmException {
    //     Map<String, Object> uploadResults = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

    //     // Generate SHA-256 hash for the image
    //     String imageHash = generateImageHash(file.getBytes());

    //     // Create a new ImageInfo object with title, description, and image hash
    //     ImageInfo imageInfo = new ImageInfo(
    //             title,
    //             description,
    //             (String) uploadResults.get("public_id"),
    //             (String) uploadResults.get("secure_url"),
    //             (String) uploadResults.get("format"),
    //             (String) uploadResults.get("created_at"),
    //             (String) uploadResults.get("resource_type"),
    //             imageHash // Store the hash of the image
    //     );

    //     // Save to the database
    //     imageInfoRepository.save(imageInfo);

    //     return imageInfo;
    // }

    // // Generate SHA-256 hash for the image
    // private String generateImageHash(byte[] imageBytes) throws NoSuchAlgorithmException {
    //     MessageDigest digest = MessageDigest.getInstance("SHA-256");
    //     byte[] hashBytes = digest.digest(imageBytes);
    //     return Base64.getEncoder().encodeToString(hashBytes); // Convert hash bytes to a Base64 string
    // }

    // public String generateImageUrl(String publicId) {
    //     return cloudinary.url().generate(publicId);
    // }

    // public String generateTransformedImageUrl(String publicId) {
    //     return cloudinary.url()
    //             .transformation(new com.cloudinary.Transformation()
    //                     .width(300)
    //                     .height(300)
    //                     .crop("fill")
    //                     .effect("grayscale"))
    //             .generate(publicId);
    // }

    // Method to search for images by content (image hash)
    // public List<ImageInfo> searchImagesByContent(String base64Hash) {
    //     // Decode the base64 string into a byte array
    //     byte[] decodedHash = Base64.getDecoder().decode(base64Hash);

    //     // Convert byte array back to base64 string if needed for database comparison
    //     String hashString = Base64.getEncoder().encodeToString(decodedHash);

    //     // Return the list of images that match the decoded hash from the database
    //     return imageInfoRepository.findByImageHash(hashString);
    // }

    // // Search images by title or description (already implemented)
    // public List<ImageInfo> searchImages(String query) {
    //     return imageInfoRepository.findByTextContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    // }

    public String generateImageHash(BufferedImage img) {
        // Resize image to 8x8
        BufferedImage resized = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, 8, 8, null);
        g.dispose();

        // Calculate average color
        long sum = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                sum += resized.getRGB(x, y) & 0xFF;
            }
        }
        long avg = sum / 64;

        // Generate hash
        StringBuilder hash = new StringBuilder();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                hash.append((resized.getRGB(x, y) & 0xFF) >= avg ? "1" : "0");
            }
        }

        return hash.toString();
    }

    public double calculateHashSimilarity(String hash1, String hash2) {
        if (hash1.length() != hash2.length()) return 0;
        
        int differences = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                differences++;
            }
        }
        
        return 1.0 - (differences / (double) hash1.length());
    }

    public void deleteImageInfo(String id) {
        imageInfoRepository.deleteById(id);
    }
}
