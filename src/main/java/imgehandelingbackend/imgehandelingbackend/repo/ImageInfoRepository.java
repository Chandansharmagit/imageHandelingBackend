package imgehandelingbackend.imgehandelingbackend.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import imgehandelingbackend.imgehandelingbackend.model.ImageInfo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageInfoRepository extends MongoRepository<ImageInfo, String> {

    Optional<ImageInfo> findById(String id);

    List<ImageInfo> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

}

/*
 * MySQL Repository (Commented out)
 * 
 * import org.springframework.data.jpa.repository.JpaRepository;
 * 
 * public interface ImageInfoRepository extends JpaRepository<ImageInfo, String> {
 *     List<ImageInfo> findByImageHash(String imageHash);
 *     List<ImageInfo> findByTextContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String text, String description);
 * }
 */
