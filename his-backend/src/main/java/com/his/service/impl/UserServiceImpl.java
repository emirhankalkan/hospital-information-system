package com.his.service.impl;

import com.his.entity.User;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.UserRepository;
import com.his.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aktif kullanıcı bulunamadı, id: " + id));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Aktif kullanıcı bulunamadı, kullanıcı adı: " + username));
    }

    @Override
    public List<User> findAllActiveUsers() {
        return userRepository.findAllByIsActive(true);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResourceAlreadyExistsException("Kullanıcı adı zaten kullanımda: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException("E-posta adresi zaten kullanımda: " + user.getEmail());
        }
        
        user.setIsActive(true); // Varsayılan olarak aktif başlasın
        
        // Şifre hashlama işlemi burada yapılmalı (örn. BCryptPasswordEncoder ile)
        // Ancak Security bağımlılıkları eklenene kadar düz metin olarak kaydedilebilir 
        // Todo eklenebilir.
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı, id: " + id));
                
        if (!user.getIsActive()) {
            throw new IllegalStateException("Kullanıcı zaten pasif durumda: " + id);
        }
        
        user.setIsActive(false); // Soft delete
        userRepository.save(user);
    }
}