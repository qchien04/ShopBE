package com.service.implement;

import com.entity.ProductImage;
import com.repository.ProductImageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductImageService {
    private final ProductImageRepository productImageRepository;

    public ProductImage save(String url) {
       ProductImage picture=ProductImage.builder().imageUrl(url).build();
       picture= productImageRepository.save(picture);
       return picture;
    }

    @Transactional
    public boolean save(List<ProductImage> pics) {
        productImageRepository.saveAll(pics);
        return true;
    }



}
