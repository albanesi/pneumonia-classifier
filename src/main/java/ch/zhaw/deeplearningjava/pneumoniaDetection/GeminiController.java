package ch.zhaw.deeplearningjava.pneumoniaDetection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class GeminiController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
    "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=%s";

    @PostMapping("/explain")
    public ResponseEntity<String> explainDiagnosis(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");

        // === Prompt an Gemini vorbereiten ===
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = String.format(GEMINI_URL, apiKey);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map contentMap = (Map) candidates.get(0).get("content");
                List<Map> parts = (List<Map>) contentMap.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    String text = (String) parts.get(0).get("text");
                    return ResponseEntity.ok(text);
                }
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gemini hat keine gültige Antwort geliefert.");

        } catch (Exception e) {
            e.printStackTrace(); // WICHTIG: Logge, was genau schief geht
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der Kommunikation mit Google Gemini: " + e.getMessage());
        }
    }
}
