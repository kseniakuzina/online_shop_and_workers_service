package com.ksu.workers.repository;

import com.ksu.workers.entities.BusynessType;
import com.ksu.workers.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUsername(String username);
    @Query("SELECT u.username, u.roles FROM User u")
    List<Object[]> findAllUsersWithRoles();
    public List<User> findAllByBusyness(BusynessType busyness);
}
