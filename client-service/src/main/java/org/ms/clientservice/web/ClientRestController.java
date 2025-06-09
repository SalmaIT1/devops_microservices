package org.ms.clientservice.web;

import lombok.RequiredArgsConstructor;
import org.ms.clientservice.entities.Client;
import org.ms.clientservice.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientRestController {
    
    private final ClientRepository clientRepository;

    // ✅ GET all clients - ADMIN seulement
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // ✅ GET mon profil client (USER connecté)
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Client> getMyProfile(Authentication authentication) {
        // Récupérer userId depuis le JWT/Authentication
        Long userId = getUserIdFromAuthentication(authentication);
        
        return clientRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ GET client by ID - ADMIN ou propriétaire
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Client> getClientById(@PathVariable Long id, Authentication authentication) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id " + id));
        
        // Si USER, vérifier qu'il accède à son propre profil
        if (hasRole(authentication, "USER")) {
            Long userId = getUserIdFromAuthentication(authentication);
            if (!client.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
        }
        
        return ResponseEntity.ok(client);
    }

    // ✅ GET clients by name - ADMIN seulement
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<Client> searchClientsByName(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        return clientRepository.findByNameContains(name, PageRequest.of(page, size));
    }

    // ✅ POST - Créer un client (lié à un user)
    @PostMapping
    public Client saveClient(@RequestBody Client client) {
        return clientRepository.save(client);
    }

    // ✅ POST - Auto-création profil client (pour USER connecté)
    @PostMapping("/register")
    public ResponseEntity<Client> registerClientProfile(
            @RequestBody Client clientData, 
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Vérifier si le client existe déjà
        if (clientRepository.findByUserId(userId).isPresent()) {
            return ResponseEntity.status(409).build(); // Conflict - déjà existant
        }
        
        // Créer le profil client
        Client client = new Client();
        client.setUserId(userId);
        client.setName(clientData.getName());
        client.setEmail(clientData.getEmail());
        
        return ResponseEntity.ok(clientRepository.save(client));
    }

    // ✅ PUT - Mettre à jour (ADMIN ou propriétaire)
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(
            @PathVariable Long id, 
            @RequestBody Client clientData,
            Authentication authentication) {
        
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id " + id));
        
        // Si USER, vérifier qu'il modifie son propre profil
        if (hasRole(authentication, "USER")) {
            Long userId = getUserIdFromAuthentication(authentication);
            if (!existingClient.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
        }
        
        existingClient.setName(clientData.getName());
        existingClient.setEmail(clientData.getEmail());
        
        return ResponseEntity.ok(clientRepository.save(existingClient));
    }

    // ✅ PUT - Mettre à jour mon profil (USER)
    @PutMapping("/me")
    public ResponseEntity<Client> updateMyProfile(
            @RequestBody Client clientData, 
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        
        return clientRepository.findByUserId(userId)
                .map(client -> {
                    client.setName(clientData.getName());
                    client.setEmail(clientData.getEmail());
                    return ResponseEntity.ok(clientRepository.save(client));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ DELETE - ADMIN seulement
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteClient(@PathVariable Long id) {
        clientRepository.deleteById(id);
    }

    // 🔧 Méthodes utilitaires
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // TODO: Extraire userId du JWT ou du principal
        // Exemple : return ((JwtAuthenticationToken) authentication).getToken().getClaim("userId");
        return 1L; // Placeholder - à adapter selon ton implémentation JWT
    }
    
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}