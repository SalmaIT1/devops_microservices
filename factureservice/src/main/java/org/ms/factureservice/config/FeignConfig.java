package org.ms.factureservice.config;

import org.ms.factureservice.feign.FeignClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Logger;
import feign.RequestInterceptor;

@Configuration
public class FeignConfig {
    
    @Autowired
    private FeignClientInterceptor feignClientInterceptor;
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return feignClientInterceptor;
    }
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Changed to FULL for better debugging
    }
}