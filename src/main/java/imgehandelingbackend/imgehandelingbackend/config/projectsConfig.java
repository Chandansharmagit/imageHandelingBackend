package imgehandelingbackend.imgehandelingbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.cloudinary.Cloudinary;
import java.util.Map;

@Configuration
public class projectsConfig {

    @Value("${config.cloudinary.cloud.key}")
    private String apiKey;

    @Value("${config.cloudinary.cloud.secret}")
    private String apiSecret;

    @Value("${config.cloudinary.cloud.name}")
    private String cloudName;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }
}
