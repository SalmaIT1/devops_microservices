package org.ms.authentificationservice.filtres;
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
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    public final String PREFIXE_JWT = "Bearer ";
    public final String CLE_SIGNATURE = "MaClé";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        System.out.println("=== JWT Authorization Filter Debug ===");
        System.out.println("Request Path: " + request.getServletPath());
        System.out.println("Request Method: " + request.getMethod());
        
        // Exclure certaines routes comme /refreshToken
        if (request.getServletPath().equals("/refreshToken") ||
        request.getServletPath().equals("/auth/register"))
        {
            System.out.println("Skipping JWT validation for /refreshToken");
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
                
                System.out.println("Authentication set in SecurityContext");
                System.out.println("=== End JWT Debug ===");
                
                filterChain.doFilter(request, response);
                
            } catch (Exception e) {
                System.out.println("JWT Processing Error: " + e.getMessage());
                e.printStackTrace();
                response.setHeader("error-message", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            System.out.println("No valid JWT token found, proceeding without authentication");
            filterChain.doFilter(request, response);
        }
    }
}