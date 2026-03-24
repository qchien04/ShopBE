package com.service;


import com.entity.Role;

import java.util.List;

public interface RoleService {
    Role saveRole(Role role);
    Role findRoleById(Integer id);
    Role findRoleByName(String name);
    void deleteRoleById(Integer id);
    List<Role> findAllRole();
}
