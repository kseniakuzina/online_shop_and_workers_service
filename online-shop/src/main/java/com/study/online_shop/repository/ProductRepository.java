package com.study.online_shop.repository;

import com.study.common.entities.ClothesType;
import com.study.common.entities.Gender;
import com.study.online_shop.entities.Product;
import com.study.online_shop.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByQuantityGreaterThan(int quantity);
    @Query("SELECT p.cost FROM Product p WHERE p.id = :id")
    Double findCostById(@Param("id") Long id);

}
