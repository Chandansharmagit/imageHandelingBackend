package imgehandelingbackend.imgehandelingbackend.OpenAiService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIservice {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String DALL_E_API_URL;

    public String generateImage(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        // Request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        // Request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);  // Number of images to generate
        requestBody.put("size", "1024x1024"); // Image resolution

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Send the request
        ResponseEntity<Map> response = restTemplate.exchange(DALL_E_API_URL, HttpMethod.POST, entity, Map.class);

        // Extract the image URL
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> firstImage = (Map<String, Object>) ((java.util.List<?>) response.getBody().get("data")).get(0);
            return firstImage.get("url").toString();
        }

        return "Failed to generate image.";
    }
}
