package com.backend.medichat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final RestTemplate restTemplate;

    @Value("${google.ai.api-key}")
    private String apiKey;


    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String getChatResponse(String userPrompt) {
        // 1. Tạo Body thủ công để tránh lỗi Map.of trên một số phiên bản Java
        Map<String, Object> textObj = Map.of("text", userPrompt);
        Map<String, Object> partsObj = Map.of("parts", List.of(textObj));
        Map<String, Object> contentsObj = Map.of("contents", List.of(partsObj));

        try {
            String finalUrl = GEMINI_API_URL + apiKey.trim();
            log.info("Gửi yêu cầu tới: {}", finalUrl);

            // 2. Sử dụng postForObject với cấu trúc Map
            Map<String, Object> response = restTemplate.postForObject(finalUrl, contentsObj, Map.class);

            return extractText(response);
        } catch (Exception e) {
            log.error("Lỗi: {}", e.getMessage());
            return "MediAI đang gặp lỗi 404 hoặc 403. Hiếu kiểm tra lại Key nhé!";
        }
    }

    private String extractText(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return parts.get(0).get("text").toString();
            }
        } catch (Exception e) {
            log.error("Lỗi bóc tách JSON: {}", e.getMessage());
        }
        return "AI không phản hồi nội dung.";
    }
}