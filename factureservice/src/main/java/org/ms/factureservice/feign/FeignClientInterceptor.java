package org.ms.factureservice.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);
    
    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            String token = getTokenFromCurrentRequest();
            if (token != null) {
                requestTemplate.header("Authorization", "Bearer " + token);
                logger.debug("Added Authorization header to Feign request: {}", requestTemplate.url());
            } else {
                logger.warn("No token found for Feign request to: {}", requestTemplate.url());
            }
        } catch (Exception e) {
            logger.error("Error adding auth header to Feign request", e);
        }
    }
    
    private String getTokenFromCurrentRequest() {
        // Try to get token from HTTP Request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        
        // Alternative: Try from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        
        return null;
    }
}
