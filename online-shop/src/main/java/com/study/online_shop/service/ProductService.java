package com.study.online_shop.service;

import com.study.common.entities.ClothesType;
import com.study.common.entities.Gender;
import com.study.common.entities.ProductDTO;
import com.study.online_shop.entities.Product;
import com.study.online_shop.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    public boolean saveProduct(ProductDTO dto) {
        Product product = new Product(dto.getName(),dto.getQuantity(),dto.getMaterial(),dto.getCost(),dto.getImage(), dto.getGender(), dto.getClothesType());
        productRepository.save(product);
        return true;
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public List<Product> getAllProductsWhereQuantityGreaterThenZero(){
        return productRepository.findByQuantityGreaterThan(0);
    }

    public Product getProductById(Long id){
        return productRepository.getReferenceById(id);
    }

    public Double getCostById(Long id){
        return productRepository.findCostById(id);
    }

    public void updateProductQuantity(Long productId){
        Product product = productRepository.getReferenceById(productId);
        product.setQuantity(100);
        productRepository.save(product);
    }

    public List<Product> getFilteredProducts(String gender, String category) {
        return productRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.greaterThan(root.get("quantity"), 0));

            if (gender != null && !gender.isEmpty()) {
                try {
                    Gender genderEnum = Gender.valueOf(gender);
                    predicates.add(cb.equal(root.get("gender"), genderEnum));
                } catch (IllegalArgumentException e) {
                }
            }

            if (category != null && !category.isEmpty()) {
                try {
                    ClothesType categoryEnum = ClothesType.valueOf(category);
                    predicates.add(cb.equal(root.get("clothesType"), categoryEnum));
                } catch (IllegalArgumentException e) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

}