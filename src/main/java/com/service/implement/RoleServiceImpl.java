package com.service.implement;

import com.entity.Role;
import com.repository.RoleRepo;
import com.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {
    private RoleRepo roleRepo;

    @Override
    @Transactional
    public Role saveRole(Role role) {
        return roleRepo.save(role);
    }

    @Override
    public Role findRoleById(Integer id) {
        Optional<Role> role=roleRepo.findById(id);
        return role.isPresent()?role.get():null;
    }

    @Override
    public Role findRoleByName(String name) {
        Optional<Role> role=roleRepo.findByName(name);
        return role.isPresent()?role.get():null;
    }

    @Override
    public void deleteRoleById(Integer id) {
        roleRepo.delete(findRoleById(id));
    }

    @Override
    public List<Role> findAllRole() {
        return roleRepo.findAll();
    }
}
