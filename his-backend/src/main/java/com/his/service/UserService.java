package com.his.service;

import com.his.entity.User;
import java.util.List;

public interface UserService {
    
    User findById(Long id);
    
    User findByUsername(String username);
    
    List<User> findAllActiveUsers();
    
    User createUser(User user);
    
    void deleteUser(Long id);
}


