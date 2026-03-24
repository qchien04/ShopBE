package com.controller;

import com.entity.ProductImage;
import com.service.implement.ProductImageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/upload")
public class StaticResourceController {

    @Value("${image.upload.dir}")
    private String UPLOAD_DIR;
    @Value("${domain}")
    private String domain;

    private final ProductImageService productImageService;
    public StaticResourceController(ProductImageService productImageService){
        this.productImageService=productImageService;
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File rỗng");
        }

        System.out.println("UPLOAD_DIR = " + UPLOAD_DIR);
        String BASE_URL = domain+"/api/images/";
        File dir = new File(UPLOAD_DIR+"/");
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR +"/"+ fileName);
        Files.write(path, file.getBytes());

        ProductImage picture=productImageService.save(BASE_URL+fileName);

        return new ResponseEntity<>(picture, HttpStatus.OK);
    }

}
