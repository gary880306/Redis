package com.example.redis.config;

import com.example.redis.utils.SystemConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/upload/**")
                .addResourceLocations("file:" + SystemConstants.IMAGE_UPLOAD_DIR);
    }
}
