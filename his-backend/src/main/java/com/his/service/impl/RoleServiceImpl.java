package com.his.service.impl;

import com.his.entity.Role;
import com.his.enums.RoleName;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.RoleRepository;
import com.his.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı, id: " + id));
    }

    @Override
    public Role findByName(RoleName name) {
        if (name == null) {
            throw new IllegalArgumentException("Rol adı boş (null) olamaz");
        }
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Belirtilen isimde rol bulunamadı: " + name));
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public Role createRole(RoleName roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Oluşturulacak rol adı boş (null) olamaz");
        }

        if (roleRepository.existsByName(roleName)) {
            throw new ResourceAlreadyExistsException("Bu rol zaten mevcut: " + roleName);
        }

        Role newRole = new Role();
        newRole.setName(roleName);
        return roleRepository.save(newRole);
    }
}
