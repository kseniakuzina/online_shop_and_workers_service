package com.study.workers.repository;

import com.study.workers.entities.BusynessType;
import com.study.workers.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUsername(String username);
    @Query("SELECT u.username, u.roles FROM User u")
    List<Object[]> findAllUsersWithRoles();
    public List<User> findAllByBusyness(BusynessType busyness);
}
