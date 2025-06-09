package org.ms.authentificationservice.web;

import org.ms.authentificationservice.dto.UserRoleData;
import org.ms.authentificationservice.entities.AppRole;
import org.ms.authentificationservice.entities.AppUser;
import org.ms.authentificationservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
public class UserServiceREST {

    private final UserService userService;
    public final String PREFIXE_JWT = "Bearer ";
    public final String CLE_SIGNATURE = "MaClé";

    // Constructor injection - Spring will inject the same BCryptPasswordEncoder bean
    public UserServiceREST(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequestMapping("/users")
    @PostAuthorize("hasAuthority('ADMIN')")
    public List<AppUser> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/add")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppUser addUser(@RequestBody AppUser user) {
        return userService.addUser(user);
    }

    @PostMapping("/roles")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppRole addRole(@RequestBody AppRole role) {
        return userService.addRole(role);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody AppUser user) {
        System.out.println("\n--- DEBUG: REGISTER ENDPOINT INITIATED ---");
        System.out.println("DEBUG: Request received for username: " + user.getUsername());
        System.out.println("DEBUG: Raw password from request: " + user.getPassword()); // !!! CAUTION: REMOVE IN PRODUCTION !!!

        try {
            // Check if user already exists
            AppUser existingUser = userService.getUserByName(user.getUsername());
            if (existingUser != null) {
                System.out.println("DEBUG: User already exists: " + user.getUsername());
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Ce nom d'utilisateur existe déjà.");
            }
            System.out.println("DEBUG: User does not exist, proceeding with registration.");

            // Initialize roles if null
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
                System.out.println("DEBUG: User roles initialized to empty HashSet.");
            } else {
                System.out.println("DEBUG: User roles already provided: " + user.getRoles().size() + " roles.");
            }

            // Save the user (assumes password is encoded in userService.addUser)
            System.out.println("DEBUG: Attempting to save user...");
            AppUser newUser = userService.addUser(user);

            // Retrieve user from DB after save (optional debug)
            AppUser userFromDbAfterSave = userService.getUserByName(newUser.getUsername());
            if (userFromDbAfterSave != null) {
                System.out.println("DEBUG: User retrieved from DB after save. ID: " + userFromDbAfterSave.getId());
                System.out.println("DEBUG: Password in DB (after save): " + userFromDbAfterSave.getPassword()); // Only for debugging
            } else {
                System.err.println("ERROR: Failed to retrieve user from DB after saving.");
            }

            // Assign default role "USER"
            try {
                System.out.println("DEBUG: Assigning default role 'USER'...");
                userService.addRoleToUser(newUser.getUsername(), "USER");
                System.out.println("DEBUG: Default role 'USER' assigned successfully.");
            } catch (Exception e) {
                System.err.println("WARNING: Could not assign default role: " + e.getMessage());
            }

            // Remove password from response
            newUser.setPassword(null);
            System.out.println("DEBUG: Password removed from response.");
            System.out.println("--- DEBUG: REGISTER ENDPOINT FINISHED SUCCESSFULLY ---");

            return ResponseEntity.ok(newUser);

        } catch (Exception e) {
            System.err.println("ERROR: Registration failed for user: " + user.getUsername());
            e.printStackTrace();
            System.err.println("--- DEBUG: REGISTER ENDPOINT FAILED ---");
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Registration failed: " + e.getMessage());
        }
    }


    @PostMapping("/addRoleToUser")
    @PostAuthorize("hasAuthority('ADMIN')")
    public void addRoleToUser(@RequestBody UserRoleData data) {
        userService.addRoleToUser(data.getUsername(), data.getRoleName());
    }

    @GetMapping(path = "/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("dans le controller.....");
        
        // récupérer le header "Authorization" (refresh-token)
        String refreshToken = request.getHeader("Authorization");
        // vérifier l'état du header
        if (refreshToken != null && refreshToken.startsWith(PREFIXE_JWT)) {
            try {
                //récupérer la valeur du refresh-token
                String jwtRefresh = refreshToken.substring(PREFIXE_JWT.length());
                //Préparer une instance du même algorithme de cryptage (HMAC256)
                Algorithm algo = Algorithm.HMAC256(CLE_SIGNATURE);
                // vérifier la validité du JWT par la vérification de sa signature
                JWTVerifier jwtVerifier = JWT.require(algo).build();
                //décoder le refresh-JWT
                DecodedJWT decodedJWT = jwtVerifier.verify(jwtRefresh);
                //récupérer la valeur de "username"
                String username = decodedJWT.getSubject();
                //Recharger l'utilisateur à partir de la BD
                AppUser user = userService.getUserByName(username);
                //récupérer la liste des rôles
                String[] roles = new String[user.getRoles().size()];
                int index = 0;
                for (AppRole r : user.getRoles()) {
                    roles[index] = r.getRoleName();
                    index++;
                }

                //Construire le access JWT
                String jwtAccessToken = JWT.create()
                    // stocker le nom de l'utilisateur
                    .withSubject(user.getUsername())
                    // date d'expiration après 1 minute
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 1000))
                    //url de la requête d'origine
                    .withIssuer(request.getRequestURL().toString())
                    //placer la liste des rôles associés à l'utilisateur courant
                    .withArrayClaim("roles", roles)
                    //signer le access JWT avec l'algorithme choisi
                    .sign(algo);
                    
                //stocker les deux tokens dans un objet HashMap
                Map<String, String> mapTokens = new HashMap<>();
                mapTokens.put("access-token", jwtAccessToken);
                mapTokens.put("refresh-token", jwtRefresh);
                //Spécifier le format du contenu de la réponse
                response.setContentType("application/json");
                //place l'objet HashMap dans le corps de la réponse
                new ObjectMapper().writeValue(response.getOutputStream(), mapTokens);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Refresh Token non disponible..");
        }
    }
}