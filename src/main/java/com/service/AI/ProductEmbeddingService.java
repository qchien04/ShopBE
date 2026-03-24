package com.service.AI;

import com.entity.AI.ProductEmbedding;
import com.entity.Product;
import com.exception.NotFoundObjectRequestException;
import com.repository.Ai.ProductEmbeddingRepository;
import com.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

    private final GeminiService geminiService;
    private final ProductEmbeddingRepository embeddingRepo;
    private final ProductRepository productRepository;

    private static final Map<String, List<String>> SYNONYMS = Map.of(
            "tụ điện", List.of("tụ", "capacitor", "cap"),
            "điện trở", List.of("trở", "resistor", "R"),
            "transistor", List.of("tranzito", "bóng bán dẫn"),
            "biến áp", List.of("biến thế", "transformer", "bế", "lõi"),
            "mosfet", List.of("mos", "fet"),
            "igbt", List.of("insulated gate bipolar transistor"),
            "ic", List.of("vi mạch", "chip"),
            "diode", List.of("điốt", "đi ốt"),
            "relay", List.of("rơ le", "rờ le", "công tắc tơ")
    );

    @Transactional
    public void embedAndSave(Product product) {
        String content = buildEnhancedContent(product);
        float[] embedding = geminiService.embed(content);

        ProductEmbedding pe = new ProductEmbedding();
        pe.setProductId(product.getId());
        pe.setContent(content);
        pe.setEmbedding(embedding);

        embeddingRepo.save(pe);
    }

    @Transactional
    public void embedAndSave(Long id) {
        Product product = productRepository.findWithDetailById(id)
                .orElseThrow(() -> new NotFoundObjectRequestException("Không tìm được sản phẩm"));

        String content = buildEnhancedContent(product);
        float[] embedding = geminiService.embed(content);

        ProductEmbedding pe = new ProductEmbedding();
        pe.setProductId(product.getId());
        pe.setContent(content);
        pe.setEmbedding(embedding);

        embeddingRepo.save(pe);
    }

    private String buildEnhancedContent(Product p) {
        StringBuilder content = new StringBuilder();

        String productName = normalize(p.getName());
        content.append("Sản phẩm: ").append(productName).append(". ");
        content.append(productName).append(". ");
        content.append(productName).append(".\n");

        if (p.getCategory() != null) {
            String categoryName = normalize(p.getCategory().getName());
            content.append("Danh mục: ").append(categoryName).append(". ");
            content.append(categoryName).append(".\n");

            content.append(getSynonyms(categoryName)).append("\n");
        }

        if (p.getBrand() != null) {
            String brandName = normalize(p.getBrand().getName());
            content.append("Thương hiệu: ").append(brandName).append(". ");
            content.append(brandName).append(".\n");
        }

        if (p.getFullDescription() != null && !p.getFullDescription().isBlank()) {
            content.append("Mô tả: ").append(normalize(p.getFullDescription())).append("\n");
        }

        content.append(extractTechnicalSpecs(p)).append("\n");

        content.append("Từ khóa: ").append(extractKeywords(p)).append("\n");

        return content.toString();
    }

    private String normalize(String text) {
        if (text == null) return "";

        return text.toLowerCase()
                .replaceAll("\\s+", " ")
                .replace("tốc", "tóc")
                .replace("lộc", "lọc")
                .replace("kẹp loa -kẹp loa", "kẹp loa")
                .replace("boar mạch", "board mạch")
                .trim();
    }

    private String getSynonyms(String text) {
        Set<String> synonyms = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : SYNONYMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                synonyms.addAll(entry.getValue());
            }
        }

        return synonyms.isEmpty() ? "" : "Còn gọi là: " + String.join(", ", synonyms);
    }

    private String extractTechnicalSpecs(Product p) {
        StringBuilder specs = new StringBuilder();
        String name = p.getName().toLowerCase();

        if (name.matches(".*\\d+w.*")) {
            specs.append("Công suất: ").append(extractPattern(name, "(\\d+)w")).append("W. ");
        }

        if (name.matches(".*\\d+v.*")) {
            specs.append("Điện áp: ").append(extractPattern(name, "(\\d+)v")).append("V. ");
        }

        if (name.matches(".*\\d+uf.*")) {
            specs.append("Dung lượng: ").append(extractPattern(name, "(\\d+)uf")).append("uF. ");
        }

        if (name.matches(".*\\d+[kω].*")) {
            specs.append("Trở kháng: ").append(extractPattern(name, "(\\d+[kω])")).append(". ");
        }

        return specs.toString();
    }

    private String extractKeywords(Product p) {
        Set<String> keywords = new HashSet<>();
        String fullText = (p.getName() + " " +
                (p.getCategory() != null ? p.getCategory().getName() : "") + " " +
                (p.getBrand() != null ? p.getBrand().getName() : "")).toLowerCase();

        String[] importantTerms = {
                "tụ", "điện trở", "transistor", "diode", "ic", "mosfet", "igbt",
                "biến áp", "relay", "led", "quạt", "nguồn", "sạc", "mạch",
                "nhôm", "tản nhiệt", "hàn", "kẹo", "lọc", "cao áp"
        };

        for (String term : importantTerms) {
            if (fullText.contains(term)) {
                keywords.add(term);
            }
        }

        return String.join(", ", keywords);
    }

    private String extractPattern(String text, String pattern) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }
}