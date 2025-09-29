package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Разрешаем все origins
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // Разрешаем все методы
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        // Разрешаем все заголовки
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Разрешаем credentials (куки, авторизацию)
        configuration.setAllowCredentials(true);
        // Устанавливаем максимальное время кеширования preflight запросов
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}