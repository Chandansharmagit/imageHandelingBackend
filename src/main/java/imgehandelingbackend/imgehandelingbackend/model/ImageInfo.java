package imgehandelingbackend.imgehandelingbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document(collection = "image_info") // MongoDB collection
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageInfo {

    @Id
    private String id;  // MongoDB will generate this if left null

    private String title;      // Title of the image
    private String description; // Description of the image
    private String imagePath;  // Path to the stored image
    private String imageHash;  // Hash of the image (if needed)
    private String text;

    // Constructor to initialize UUID if needed
    public ImageInfo(String title, String description, String imagePath, String imageHash, String text) {
        this.id = java.util.UUID.randomUUID().toString(); // Generates a new UUID
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.imageHash = imageHash;
        this.text = text;
    }
}
