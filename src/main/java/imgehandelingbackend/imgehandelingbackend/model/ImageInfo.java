package imgehandelingbackend.imgehandelingbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.util.UUID;

@Document(collection = "image_info") // MongoDB collection
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageInfo {

    @Id
    private String id = UUID.randomUUID().toString();  // Auto-generated UUID string

    private String title;      // Title of the image
    private String description; // Description of the image
    private String imagePath;  // Path to the stored image
    private String imageHash;  // Hash of the image (if needed)
    private String text;
  
    public String getImageHash() {
        return imageHash;
    }
    
    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }
}
