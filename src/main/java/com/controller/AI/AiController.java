package com.controller.AI;

import com.service.AI.AiResponse;
import com.service.AI.ProductEmbeddingService;
import com.service.AI.ProductRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final ProductRagService ragService;
    private final ProductEmbeddingService productEmbeddingService;
    @GetMapping("/ask")
    public ResponseEntity<AiResponse> ask(@RequestParam String q) {
        AiResponse res=ragService.ask(q);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    //private final ProductRagService ragService;

    @GetMapping("/emb")
    public ResponseEntity<String> ask(@RequestParam Long id) {
        productEmbeddingService.embedAndSave(id);
        return new ResponseEntity<String>("Ok", HttpStatus.OK);
    }
}
