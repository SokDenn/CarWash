package org.example.service;

import org.example.model.Role;
import org.example.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired
    RoleRepo roleRepo;

    public List<Role> findAll() {
        return (List<Role>) roleRepo.findAll();
    }
}
