package com.his.config;

import com.his.entity.Role;
import com.his.enums.RoleName;
import com.his.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            log.info("Veritabanında rol bulunamadı. Temel roller ekleniyor...");
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            });
            log.info("Roller başarıyla eklendi.");
        }
    }
}
