package com.backend.medichat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final RestTemplate restTemplate;

    @Value("${GEMINI_API_KEY}")
    private String apiKeysString;

    private List<String> apiKeys;
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    // Sử dụng Gemini 1.5 Flash - Phiên bản tối ưu nhất hiện nay
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    // Danh sách lưu trữ toàn bộ câu hỏi và câu trả lời y tế trong RAM
    private List<Map<String, String>> localKnowledgeBase = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 1. Nạp Key Gemini
        if (apiKeysString != null && !apiKeysString.isEmpty()) {
            this.apiKeys = Arrays.asList(apiKeysString.split(","));
            log.info(">>> [MediAI] Hệ thống nạp thành công {} API Keys.", apiKeys.size());
        } else {
            log.error(">>> [LỖI] Không tìm thấy GEMINI_API_KEY trong file cấu hình!");
        }

        // 2. Nạp dữ liệu ViHealthQA từ file JSON nội bộ
        loadLocalMedicalData();
    }

    /**
     * Nạp dữ liệu từ src/main/resources/vihealthqa.json vào RAM
     * SỬ DỤNG CLASSLOADER ĐỂ TRÁNH LỖI NULL POINTER EXCEPTION
     */
//    private void loadLocalMedicalData() {
//        try {
//            log.info(">>> [System] Đang nạp cơ sở tri thức y khoa nội bộ...");
//
//            // Cách đọc file an toàn nhất trong Spring Boot
//            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("vihealthqa.json");
//
//            if (inputStream == null) {
//                log.error(">>> [LỖI NGHIÊM TRỌNG] Không tìm thấy file 'vihealthqa.json' trong thư mục resources!");
//                return; // Dừng việc đọc file nếu không thấy
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//
//            // Đọc toàn bộ nội dung file JSON thành dạng cây
//            JsonNode rootNode = mapper.readTree(inputStream);
//
//            // Tìm node "rows" chứa dữ liệu thực tế
//            JsonNode rowsNode = rootNode.get("rows");
//
//            if (rowsNode != null && rowsNode.isArray()) {
//                for (JsonNode rowItem : rowsNode) {
//                    JsonNode rowData = rowItem.get("row");
//
//                    if (rowData != null) {
//                        String q = rowData.has("question") ? rowData.get("question").asText().toLowerCase() : "";
//                        String a = rowData.has("answer") ? rowData.get("answer").asText() : "";
//
//
//                        String link = rowData.has("link") ? rowData.get("link").asText() : "";
//
//                        if (!q.isEmpty() && !a.isEmpty()) {
//                            // LƯU CẢ LINK VÀO RAM
//                            localKnowledgeBase.add(Map.of("question", q, "answer", a, "link", link));
//                        }
//                    }
//                }
//                log.info(">>> [System] Đã nạp thành công {} bộ câu hỏi/đáp y khoa vào RAM.", localKnowledgeBase.size());
//            } else {
//                log.warn(">>> [System] File JSON không chứa thuộc tính 'rows'. Vui lòng kiểm tra lại cấu trúc file.");
//            }
//        } catch (Exception e) {
//            log.error(">>> [Lỗi nạp dữ liệu] Không thể đọc file vihealthqa.json: {}", e.getMessage());
//        }
//    }

    private void loadLocalMedicalData() {
        // 1. Khai báo danh sách các file JSON cần đọc (Thêm bao nhiêu file tùy ý)
        String[] jsonFiles = {
                "vihealthqa.json",
                "vihealthqa2.json",
                "a1.json" // Duy có thể tự tạo thêm các file này trong resources
        };

        log.info(">>> [System] Bắt đầu nạp cơ sở tri thức y khoa từ {} file...", jsonFiles.length);
        ObjectMapper mapper = new ObjectMapper();

        // 2. Lặp qua từng file để nạp dữ liệu
        for (String fileName : jsonFiles) {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

                // Nếu file không tồn tại, chỉ cảnh báo rồi chạy tiếp file khác (không bị sập)
                if (inputStream == null) {
                    log.warn(">>> [CẢNH BÁO] Không tìm thấy file '{}'. Đang bỏ qua...", fileName);
                    continue;
                }

                JsonNode rootNode = mapper.readTree(inputStream);
                JsonNode rowsNode = rootNode.get("rows");

                if (rowsNode != null && rowsNode.isArray()) {
                    int count = 0; // Đếm số câu hỏi trong từng file
                    for (JsonNode rowItem : rowsNode) {
                        JsonNode rowData = rowItem.get("row");

                        if (rowData != null) {
                            String q = rowData.has("question") ? rowData.get("question").asText().toLowerCase() : "";
                            String a = rowData.has("answer") ? rowData.get("answer").asText() : "";
                            String link = rowData.has("link") ? rowData.get("link").asText() : "";

                            if (!q.isEmpty() && !a.isEmpty()) {
                                // Gộp tất cả vào một List chung duy nhất
                                localKnowledgeBase.add(Map.of("question", q, "answer", a, "link", link));
                                count++;
                            }
                        }
                    }
                    log.info(">>> [System] Đã nạp thành công {} bản ghi từ file '{}'.", count, fileName);
                } else {
                    log.warn(">>> [System] File '{}' không đúng cấu trúc (thiếu 'rows').", fileName);
                }
            } catch (Exception e) {
                log.error(">>> [Lỗi] Không thể đọc file '{}': {}", fileName, e.getMessage());
            }
        }

        // Tổng kết sau khi chạy xong tất cả các file
        log.info(">>> [System] HOÀN TẤT! Tổng cộng có {} bộ câu hỏi/đáp sẵn sàng trong RAM.", localKnowledgeBase.size());
    }

    /**
     * Tìm kiếm siêu tốc trên RAM (Offline RAG)
     */
    private String fetchExpertKnowledge(String userQuery) {
        String queryLower = userQuery.toLowerCase();
        log.info(">>> [RAG] Đang tra cứu cơ sở dữ liệu nội bộ...");

        for (Map<String, String> entry : localKnowledgeBase) {
            String question = entry.get("question");
            if (question.contains(queryLower) || queryLower.contains(question)) {
                log.info(">>> [RAG] Tìm thấy kết quả khớp trong Local DB.");

                // ÉP CẢ NỘI DUNG VÀ LINK VÀO 1 CHUỖI
                String answer = entry.get("answer");
                String link = entry.get("link");

                // Nếu có link thì đính kèm, không thì thôi
                if (link != null && !link.isEmpty()) {
                    return answer + "\n[Nguồn tham khảo: " + link + "]";
                }
                return answer;
            }
        }

        log.info(">>> [RAG] Không tìm thấy dữ liệu đặc thù. Sử dụng Lớp bảo vệ cơ bản.");
        if (queryLower.contains("sốt xuất huyết")) {
            return "Sốt xuất huyết là bệnh do muỗi vằn truyền... [Nguồn tham khảo: Kiến thức Y khoa VN]";
        }
        return "Hãy tư vấn chi tiết dựa trên kiến thức y khoa chuẩn xác của bạn.";
    }

    /**
     * Xoay vòng API Key
     */
    private String getNextApiKey() {
        if (apiKeys == null || apiKeys.isEmpty()) throw new RuntimeException("Chưa cấu hình API Key");
        int index = Math.abs(currentKeyIndex.getAndIncrement() % apiKeys.size());
        return apiKeys.get(index).trim();
    }

    /**
     * Tổng hợp Prompt và Gọi Gemini AI
     */
    public String getChatResponse(String userPrompt) {
        try {
            String expertData = fetchExpertKnowledge(userPrompt);

            String enrichedPrompt = String.format(
                    "Bạn là MediAI, chuyên gia y tế.\n" +
                            "Dựa trên dữ liệu chuẩn sau: [%s]\n" +
                            "Trả lời câu hỏi: \"%s\"\n" +
                            "Yêu cầu:\n" +
                            "1. Dùng Markdown trình bày cho rõ ràng (danh sách, in đậm).\n" +
                            "2. Lời lẽ ân cần, nhắc người bệnh đi viện nếu cần.\n" +
                            "3. BẮT BUỘC: Nếu trong dữ liệu chuẩn có phần [Nguồn tham khảo: ...], bạn PHẢI trích dẫn nguyên văn đường link đó ở cuối cùng của câu trả lời với định dạng: \n\n**Nguồn tham khảo:** [đường link].",
                    expertData, userPrompt
            );

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", enrichedPrompt))))
            );

            String activeKey = getNextApiKey();
            String finalUrl = GEMINI_API_URL + activeKey;

            log.info(">>> [Gemini] Gọi AI với Key index: {}", (currentKeyIndex.get() - 1) % apiKeys.size());
            Map<String, Object> response = restTemplate.postForObject(finalUrl, body, Map.class);

            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error(">>> [Lỗi] Kết nối AI thất bại: {}", e.getMessage());
            return "MediAI đang bảo trì định kỳ. Quý khách vui lòng thử lại sau ít phút nhé!";
        }
    }

    /**
     * Parse kết quả trả về từ Google Gemini
     */
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return parts.get(0).get("text").toString();
            }
        } catch (Exception e) {
            log.error(">>> [Parse Error] Không thể đọc nội dung trả về từ AI.");
        }
        return "Xin lỗi, tôi không thể hiển thị câu trả lời lúc này.";
    }


}