package com.memposit.esigning.dokobit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Web mvc config.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
   public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/*")
               .allowedOriginPatterns("*")
               .allowedMethods("*")
               .allowedHeaders("*")
               .allowCredentials(true);
    }
}
