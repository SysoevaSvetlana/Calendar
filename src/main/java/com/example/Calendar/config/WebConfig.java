//package com.example.Calendar.config;
//
//
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:3000")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
//                .exposedHeaders("Authorization")
//                .allowedHeaders("Content-Type", "X-KL-kfa-Ajax-Request", "*")
//                .exposedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600); // кэш preflight
//    }
//}
