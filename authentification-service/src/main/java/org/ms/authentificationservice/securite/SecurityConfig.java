package org.ms.authentificationservice.securite;

import org.ms.authentificationservice.entities.AppUser;
import org.ms.authentificationservice.filtres.JwtAuthenticationFilter;
import org.ms.authentificationservice.filtres.JwtAuthorizationFilter;
import org.ms.authentificationservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    
    private final UserService userService;
    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(UserService userService, AuthenticationConfiguration authenticationConfiguration) {
        this.userService = userService;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Autowired
    public void globalConfig(AuthenticationManagerBuilder auth) throws Exception {
        // utiliser les données de la BD
        auth.userDetailsService(new UserDetailsService() {
            @Override
            // Cette méthode est appelée suite à la validation du formulaire d'authentification
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // récupérer la valeur de "username" pour récupérer un objet AppUser de la BD
                AppUser appUser = userService.getUserByName(username);
                // construire une collection des rôles (permissions) selon le format de SpringSecurity
                Collection<GrantedAuthority> permissions = new ArrayList<>();
                // parcourir la liste des rôles de l'utilisateur pour remplir la collection des permissions
                appUser.getRoles().forEach(r -> {
                    permissions.add(new SimpleGrantedAuthority(r.getRoleName()));
                });
                // retourner un objet "User" de Spring Framework qui contient: "username", "password" et les permissions
                return new User(appUser.getUsername(), appUser.getPassword(), permissions);
            }
        });
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Completely disable CSRF
        http.csrf().disable();
        
        // Disable sessions - use stateless JWT
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        // Disable frame options for H2 console
        http.headers().frameOptions().disable();

        // Configure authorization - ORDER MATTERS!
        http.authorizeRequests()
            // Public endpoints FIRST
            .requestMatchers("/auth/**", "/refreshToken").permitAll()
            // Protected endpoints
            .requestMatchers(HttpMethod.POST, "/users/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.GET, "/users/**").hasAuthority("USER")
            // All other requests require authentication
            .anyRequest().authenticated();

        // Create and configure JWT authentication filter
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
            authenticationManager(authenticationConfiguration), 
            userService // Pass UserService to the filter
        );
        
        // Add JWT filters
        http.addFilter(jwtAuthenticationFilter);
        http.addFilterBefore(new JwtAuthorizationFilter(), 
            UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}