package org.ms.clientservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    public final String PREFIXE_JWT = "Bearer ";
    public final String CLE_SIGNATURE = "MaClé";
    
    // Define public endpoints that don't need JWT authentication
    private final List<RequestMatcher> publicEndpoints = Arrays.asList(
        new AntPathRequestMatcher("/refreshToken", "POST"),
        new AntPathRequestMatcher("/clients", "POST")  // Registration endpoint
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        System.out.println("=== JWT Authorization Filter Debug ===");
        System.out.println("Request Path: " + request.getServletPath());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Full URI: " + request.getRequestURI());
        
        // Check if this is a public endpoint that should skip JWT validation
        if (isPublicEndpoint(request)) {
            System.out.println("✅ SKIPPING JWT validation for public endpoint: " + request.getMethod() + " " + request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }
        
        String authorizationToken = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authorizationToken);
        
        if (authorizationToken != null && authorizationToken.startsWith(PREFIXE_JWT)) {
            try {
                String jwt = authorizationToken.substring(PREFIXE_JWT.length());
                System.out.println("JWT Token (first 50 chars): " + jwt.substring(0, Math.min(50, jwt.length())) + "...");
                
                Algorithm algo = Algorithm.HMAC256(CLE_SIGNATURE);
                JWTVerifier jwtVerifier = JWT.require(algo).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwt);
                
                String username = decodedJWT.getSubject();
                System.out.println("Username from JWT: " + username);
                
                String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                System.out.println("Roles from JWT: " + Arrays.toString(roles));
                
                Collection<GrantedAuthority> permissions = new ArrayList<>();
                if (roles != null) {
                    for (String role : roles) {
                        permissions.add(new SimpleGrantedAuthority(role));
                        System.out.println("Added authority: " + role);
                    }
                } else {
                    System.out.println("WARNING: No roles found in JWT!");
                }
                
                System.out.println("Final authorities: " + permissions);
                
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, permissions);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                
                System.out.println("✅ Authentication set in SecurityContext");
                System.out.println("=== End JWT Debug ===");
                
                filterChain.doFilter(request, response);
                
            } catch (Exception e) {
                System.out.println("❌ JWT Processing Error: " + e.getMessage());
                e.printStackTrace();
                response.setHeader("error-message", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token");
            }
        } else {
            System.out.println("❌ No valid JWT token found - access denied");
            System.out.println("Expected: Authorization: Bearer <token>");
            System.out.println("Received: " + authorizationToken);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication required - missing or invalid Authorization header");
        }
    }
    
    /**
     * Check if the current request matches any public endpoint that should skip JWT validation
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        return publicEndpoints.stream()
                .anyMatch(matcher -> matcher.matches(request));
    }
}