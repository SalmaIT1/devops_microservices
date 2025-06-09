package org.ms.authentificationservice.filtres;

import org.ms.authentificationservice.entities.AppUser;
import org.ms.authentificationservice.services.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private UserService userService; // Add UserService

    // Updated constructor to accept UserService
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService; // Inject UserService
        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        System.out.println("attemptAuthentication");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("Received username: " + username);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                username, password);
        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication");
        
        // Get the authenticated user from Spring Security
        User user = (User) authResult.getPrincipal();
        String username = user.getUsername();
        
        // Get the complete AppUser from database to access the ID
        AppUser appUser = userService.getUserByName(username);
        Long userId = null;
        if (appUser != null) {
            userId = appUser.getId(); // Assuming your AppUser entity has getId() method
            System.out.println("Found AppUser with ID: " + userId);
        } else {
            System.out.println("WARNING: AppUser not found for username: " + username);
        }
        
        // Get roles from Spring Security user
        String[] roles = new String[user.getAuthorities().size()];
        int index = 0;
        for (GrantedAuthority gi : user.getAuthorities()) {
            roles[index] = gi.toString();
            index++;
        }
        
        // Create JWT tokens
        Algorithm algo = Algorithm.HMAC256("MaClé");
        
        String jwtAccessToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .withArrayClaim("roles", roles)
                .sign(algo);
                
        String jwtRefreshToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algo);
        
        // Build response with user ID
        Map<String, Object> mapTokens = new HashMap<>();
        mapTokens.put("access-token", jwtAccessToken);
        mapTokens.put("refresh-token", jwtRefreshToken);
        mapTokens.put("username", username);
        mapTokens.put("roles", roles);
        
        // Add the user ID to the response
        if (userId != null) {
            mapTokens.put("id", userId);
            mapTokens.put("userId", userId); // Alternative field name for compatibility
        }
        
        System.out.println("Login response will include user ID: " + userId);
        
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), mapTokens);
    }
}