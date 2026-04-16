package com.his.service.impl;

import com.his.entity.User;
import com.his.repository.UserRepository;
import com.his.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public List<User> findAllActiveUsers() {
        return null;
    }

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }
}