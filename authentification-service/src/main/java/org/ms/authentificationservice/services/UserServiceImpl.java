package org.ms.authentificationservice.services;

import lombok.*;
import org.ms.authentificationservice.entities.AppRole;
import org.ms.authentificationservice.entities.AppUser;
import org.ms.authentificationservice.repositories.AppRoleRepository;
import org.ms.authentificationservice.repositories.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {
	 private final AppUserRepository appUserRepository;
	 private final AppRoleRepository appRoleRepository;
	 private final PasswordEncoder passwordEncoder; // ✅ Correctly declared


	@Override
	public AppUser addUser(AppUser appUser) {
		appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
		return appUserRepository.save(appUser);
		}

	@Override
	public AppRole addRole(AppRole appRole) {
		return appRoleRepository.save(appRole);
		}

	@Override
	public void addRoleToUser(String username, String roleName) {
		AppUser user = appUserRepository.findByUsername(username);
		AppRole role = appRoleRepository.findByRoleName(roleName);
		if (user != null && role != null) {
		user.getRoles().add(role);}
		
	}

	@Override
	public AppUser getUserByName(String username) {
		// TODO Auto-generated method stub
		return appUserRepository.findByUsername(username);
		}

	@Override
	public List<AppUser> getAllUsers() {
		// TODO Auto-generated method stub
		return appUserRepository.findAll();
	}


}
