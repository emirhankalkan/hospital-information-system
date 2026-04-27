package com.his.service.impl;

import com.his.dto.request.LoginRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.response.JwtResponse;
import com.his.entity.Role;
import com.his.entity.User;
import com.his.enums.RoleName;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.RoleRepository;
import com.his.repository.UserRepository;
import com.his.security.CustomUserDetails;
import com.his.security.JwtUtils;
import com.his.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(
                jwt,
                userDetails.getUser().getId(),
                userDetails.getUsername(),
                userDetails.getUser().getEmail(),
                roles
        );
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Hata: Kullanıcı adı zaten kullanımda!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Hata: E-posta adresi zaten kullanımda!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        Set<String> strRoles = request.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                    .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (PATIENT)."));
            roles.add(patientRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                                .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (ADMIN)."));
                        roles.add(adminRole);
                        break;
                    case "receptionist":
                        Role reqRole = roleRepository.findByName(RoleName.RECEPTIONIST)
                                .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (RECEPTIONIST)."));
                        roles.add(reqRole);
                        break;
                    case "doctor":
                        Role docRole = roleRepository.findByName(RoleName.DOCTOR)
                                .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (DOCTOR)."));
                        roles.add(docRole);
                        break;
                    default:
                        Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                                .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (PATIENT)."));
                        roles.add(patientRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
    }
}
