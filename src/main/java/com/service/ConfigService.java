package com.service;

import com.DTO.HomePageConfigDTO;
import com.DTO.ShippingConfigDTO;
import com.entity.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    private static final String BANNER_KEY = "banner";

    public HomePageConfigDTO getBannerConfig() {
        return configRepository.findByConfigKey(BANNER_KEY)
                .map(c -> {
                    try {
                        return objectMapper.readValue(c.getConfigValue(), HomePageConfigDTO.class);
                    } catch (Exception e) {
                        return new HomePageConfigDTO();
                    }
                })
                .orElse(new HomePageConfigDTO()); // trả về rỗng → FE dùng fallback
    }

    public HomePageConfigDTO saveBannerConfig(HomePageConfigDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            Config config = configRepository.findByConfigKey(BANNER_KEY)
                    .orElse(Config.builder().configKey(BANNER_KEY).build());
            config.setConfigValue(json);
            configRepository.save(config);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu banner config", e);
        }
    }

    private static final String SHIPPING_KEY = "shipping_config";

    public ShippingConfigDTO getShippingConfig() {
        return configRepository.findByConfigKey(SHIPPING_KEY)
                .map(c -> {
                    try {
                        return objectMapper.readValue(c.getConfigValue(), ShippingConfigDTO.class);
                    } catch (Exception e) {
                        return new ShippingConfigDTO();
                    }
                })
                .orElse(new ShippingConfigDTO());
    }

    public ShippingConfigDTO saveShippingConfig(ShippingConfigDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            Config config = configRepository.findByConfigKey(SHIPPING_KEY)
                    .orElse(Config.builder().configKey(SHIPPING_KEY).build());
            config.setConfigValue(json);
            configRepository.save(config);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu shipping config", e);
        }
    }
}