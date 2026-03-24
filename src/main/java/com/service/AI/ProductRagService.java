package com.service.AI;

import com.DTO.ProductEmbeddingProjection;
import com.entity.Product;
import com.repository.Ai.ProductEmbeddingRepository;
import com.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductRagService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ProductEmbeddingRepository embeddingRepo;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepo;

    public AiResponse ask(String question) {
        try {
            System.out.println("=== Embedding question ===");
            System.out.println("Question: " + question);

            float[] queryEmbedding = geminiService.embedQuest(question);
            System.out.println("Embedding size: " + queryEmbedding.length);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < queryEmbedding.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(queryEmbedding[i]);
            }
            sb.append("]");
            String embeddingStr = sb.toString();

            List<ProductEmbeddingProjection> embeddings = embeddingRepo.searchSimilar(embeddingStr, 10);

            if (embeddings.isEmpty()) {
                throw new Exception("Empty text!");
            }

            List<Product> products = embeddings.stream()
                    .map(e -> {
                        System.out.println("Loading product ID: " + e.getProductId());
                        return productRepo.findById(e.getProductId()).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                throw new Exception("Empty products!");
            }

            StringBuilder context = new StringBuilder();
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                context.append(String.format("""
                    %d. Sản phẩm: %s
                       Mô tả ngắn: %s
                       Giá: %,.0f VNĐ
                       Link: /products/%s
                    ---
                    """,
                        (i + 1),
                        p.getName() != null ? p.getName() : "N/A",
                        p.getShortDescription() != null ? p.getShortDescription() : "N/A",
                        p.getSalePrice() != null ? p.getSalePrice() : 0.0,
                        p.getSlug() != null ? p.getSlug() : ""
                ));
            }

            String prompt = String.format("""
                Bạn là chuyên gia tư vấn sản phẩm. Dựa trên thông tin sản phẩm bên dưới, hãy trả lời câu hỏi của khách hàng.
                
                THÔNG TIN SẢN PHẨM:
                %s
                
                CÂU HỎI KHÁCH HÀNG: "%s"
                
                Trả về JSON theo đúng format sau, KHÔNG thêm markdown, KHÔNG thêm ```json:
                {
                  "message": "Lời tư vấn ngắn gọn, thân thiện (2-3 câu)",
                  "products": [
                    {
                      "name": "Tên sản phẩm",
                      "reason": "Lý do phù hợp với nhu cầu khách hàng",
                      "price": 123000,
                      "link": "/products/slug"
                    }
                  ],
                  "note": "Ghi chú thêm nếu có, hoặc null"
                }
                
                Chỉ liệt kê sản phẩm thực sự phù hợp. Trả lời bằng tiếng Việt.
                """,
                context.toString(),
                question
            );

            String rawAnswer = geminiService.chat(prompt);

            String cleaned = rawAnswer
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();
                System.out.println(cleaned);
            return objectMapper.readValue(cleaned, AiResponse.class);

        } catch (Exception e) {
            AiResponse fallback = new AiResponse();
            fallback.setMessage("Xin lỗi, đã xảy ra lỗi. Vui lòng thử lại sau.");
            fallback.setProducts(List.of());
            return fallback;

        }
    }
}