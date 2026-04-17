package com.his.service;

import com.his.entity.Role;
import com.his.enums.RoleName;

import java.util.List;

public interface RoleService {

    Role findById(Long id);

    Role findByName(RoleName name);

    List<Role> findAll();

    Role createRole(RoleName roleName);
}
