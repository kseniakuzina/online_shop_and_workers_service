package com.study.online_shop.repository;


import com.study.common.entities.CartStatus;
import com.study.online_shop.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Long> {
    public List<Cart> findAllByUserId(Long userId);
    public List<Cart> findAllByUserIdAndStatus(Long userId, CartStatus Status);
    List<Cart> findAllByUserAndProduct(User user, Product product);
    Cart findByUserAndProductAndStatus(User user, Product product, CartStatus status);
}
