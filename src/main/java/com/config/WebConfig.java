package com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${image.upload.dir}")
    private String uploadDir;

    @Bean
    public PayOS payOS() {
        ClientOptions options =
                ClientOptions.builder()
                        .clientId(clientId)
                        .apiKey(apiKey)
                        .checksumKey(checksumKey)
                        .logLevel(ClientOptions.LogLevel.INFO)
                        .build();
        return new PayOS(options);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đảm bảo path luôn có file:// prefix và trailing slash
        String location = uploadDir.startsWith("file:")
                ? uploadDir
                : "file:" + uploadDir;

        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
