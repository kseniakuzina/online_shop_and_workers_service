package com.ksu.workers.repository;

import com.ksu.workers.entities.Role;
import com.ksu.workers.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByName(String name);
}
