package com.study.online_shop.service;

import com.study.common.entities.ClothesType;
import com.study.common.entities.Gender;
import com.study.common.entities.ProductDTO;
import com.study.online_shop.entities.Product;
import com.study.online_shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setQuantity(10);
        testProduct.setMaterial("Wood");
        testProduct.setCost(100.0);
        testProduct.setImage("image.jpg");
        testProduct.setGender(Gender.MALE);
        testProduct.setClothesType(ClothesType.OUTERWEAR);

        testProductDTO = new ProductDTO();
        testProductDTO.setName("Test Product");
        testProductDTO.setQuantity(10);
        testProductDTO.setMaterial("Wood");
        testProductDTO.setCost(100.0);
        testProductDTO.setImage("image.jpg");
        testProductDTO.setGender(Gender.MALE);
        testProductDTO.setClothesType(ClothesType.OUTERWEAR);
    }

    @Test
    void saveProduct_ShouldReturnTrue() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        boolean result = productService.saveProduct(testProductDTO);

        // Assert
        assertTrue(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void saveProduct_ShouldCreateProductWithCorrectFields() {
        // Arrange
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.saveProduct(testProductDTO);

        // Assert
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertEquals("Test Product", savedProduct.getName());
        assertEquals(10, savedProduct.getQuantity());
        assertEquals("Wood", savedProduct.getMaterial());
        assertEquals(100.0, savedProduct.getCost());
        assertEquals("image.jpg", savedProduct.getImage());
        assertEquals(Gender.MALE, savedProduct.getGender());
        assertEquals(ClothesType.OUTERWEAR, savedProduct.getClothesType());
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertEquals(expectedProducts, result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllProducts_ShouldReturnEmptyList_WhenNoProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllProductsWhereQuantityGreaterThenZero_ShouldReturnFilteredProducts() {
        // Arrange
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findByQuantityGreaterThan(0)).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getAllProductsWhereQuantityGreaterThenZero();

        // Assert
        assertEquals(expectedProducts, result);
        assertEquals(1, result.size());
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Arrange
        when(productRepository.getReferenceById(1L)).thenReturn(testProduct);

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertEquals(testProduct, result);
    }

    @Test
    void getCostById_ShouldReturnCost() {
        // Arrange
        when(productRepository.findCostById(1L)).thenReturn(100.0);

        // Act
        Double result = productService.getCostById(1L);

        // Assert
        assertEquals(100.0, result);
    }

    @Test
    void updateProductQuantity_ShouldSetQuantityTo100() {
        // Arrange
        testProduct.setQuantity(50);
        when(productRepository.getReferenceById(1L)).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.updateProductQuantity(1L);

        // Assert
        assertEquals(100, testProduct.getQuantity());
        verify(productRepository).save(testProduct);
    }

    @Test
    void getFilteredProducts_WithBothFilters_ShouldApplyBothPredicates() {
        // Arrange
        String gender = "MALE";
        String category = "OUTERWEAR";
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts(gender, category);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void getFilteredProducts_WithOnlyGender_ShouldApplyGenderPredicate() {
        // Arrange
        String gender = "MALE";
        String category = null;
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts(gender, category);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void getFilteredProducts_WithOnlyCategory_ShouldApplyCategoryPredicate() {
        // Arrange
        String gender = null;
        String category = "OUTERWEAR";
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts(gender, category);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void getFilteredProducts_WithNoFilters_ShouldReturnAllWithQuantityGreaterThanZero() {
        // Arrange
        String gender = null;
        String category = null;
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts(gender, category);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void getFilteredProducts_WithEmptyStrings_ShouldIgnoreFilters() {
        // Arrange
        String gender = "";
        String category = "";
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts(gender, category);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void getFilteredProducts_WithInvalidEnumValues_ShouldIgnoreInvalidFilters() {
        // Arrange
        String gender = "INVALID_GENDER";
        String category = "INVALID_CATEGORY";
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts(gender, category);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void getFilteredProducts_ShouldFilterByMultipleProducts() {
        // Arrange
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setQuantity(5);
        product2.setGender(Gender.MALE);
        product2.setClothesType(ClothesType.OUTERWEAR);

        List<Product> expectedProducts = Arrays.asList(testProduct, product2);
        when(productRepository.findAll(any(Specification.class))).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getFilteredProducts("MALE", "OUTERWEAR");

        // Assert
        assertEquals(2, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }
}