package com.service;

import com.DTO.BannerConfigDTO;
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

    public BannerConfigDTO getBannerConfig() {
        return configRepository.findByConfigKey(BANNER_KEY)
                .map(c -> {
                    try {
                        return objectMapper.readValue(c.getConfigValue(), BannerConfigDTO.class);
                    } catch (Exception e) {
                        return new BannerConfigDTO();
                    }
                })
                .orElse(new BannerConfigDTO()); // trả về rỗng → FE dùng fallback
    }

    public BannerConfigDTO saveBannerConfig(BannerConfigDTO dto) {
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
}