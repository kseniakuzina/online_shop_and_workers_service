package com.study.online_shop.service;

import com.study.common.entities.CartStatus;
import com.study.online_shop.entities.Cart;
import com.study.online_shop.entities.Product;
import com.study.online_shop.entities.User;
import com.study.online_shop.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setCost(100.0);

        testCart = new Cart(testUser, testProduct);
        testCart.setId(1L);
    }

    @Test
    void saveCart_ShouldSaveNewCart_WhenNoExistingCartInCart() {
        // Arrange
        Long productId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findAllByUserAndProduct(testUser, testProduct))
                .thenReturn(Collections.emptyList());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        boolean result = cartService.saveCart(productId);

        // Assert
        assertTrue(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void saveCart_ShouldReturnFalse_WhenExistingCartInCart() {
        // Arrange
        Long productId = 1L;
        List<Cart> existingCarts = new ArrayList<>();
        Cart existingCart = new Cart(testUser, testProduct);
        existingCart.setStatus(CartStatus.IN_CART);
        existingCarts.add(existingCart);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findAllByUserAndProduct(testUser, testProduct))
                .thenReturn(existingCarts);

        // Act
        boolean result = cartService.saveCart(productId);

        // Assert
        assertFalse(result);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void saveCart_ShouldSaveNewCart_WhenExistingCartNotInCart() {
        // Arrange
        Long productId = 1L;
        List<Cart> existingCarts = new ArrayList<>();
        Cart existingCart = new Cart(testUser, testProduct);
        existingCart.setStatus(CartStatus.IN_ORDER);
        existingCarts.add(existingCart);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findAllByUserAndProduct(testUser, testProduct))
                .thenReturn(existingCarts);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        boolean result = cartService.saveCart(productId);

        // Assert
        assertTrue(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getAllCarts_ShouldReturnAllCarts() {
        // Arrange
        List<Cart> expectedCarts = Collections.singletonList(testCart);
        when(cartRepository.findAll()).thenReturn(expectedCarts);

        // Act
        List<Cart> result = cartService.getAllCarts();

        // Assert
        assertEquals(expectedCarts, result);
        assertEquals(1, result.size());
        verify(cartRepository).findAll();
    }

    @Test
    void getAllCartsByUserId_ShouldReturnCartsByUserId() {
        // Arrange
        Long userId = 1L;
        List<Cart> expectedCarts = Collections.singletonList(testCart);
        when(cartRepository.findAllById(Collections.singleton(userId)))
                .thenReturn(expectedCarts);

        // Act
        List<Cart> result = cartService.getAllCartsByUserId(userId);

        // Assert
        assertEquals(expectedCarts, result);
        verify(cartRepository).findAllById(Collections.singleton(userId));
    }

    @Test
    void getAllProductsByUserId_ShouldReturnOnlyInCartProducts() {
        // Arrange
        Long userId = 1L;
        List<Cart> carts = new ArrayList<>();

        Cart inCartCart = new Cart(testUser, testProduct);
        inCartCart.setStatus(CartStatus.IN_CART);
        inCartCart.setProductQuantity(2);
        inCartCart.setAmount(200.0);
        carts.add(inCartCart);

        Cart orderedCart = new Cart(testUser, testProduct);
        orderedCart.setStatus(CartStatus.IN_ORDER);
        carts.add(orderedCart);

        when(cartRepository.findAllByUserId(userId)).thenReturn(carts);

        // Act
        List<Pair<Product, Pair<Integer, Double>>> result =
                cartService.getAllProductsByUserId(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0).getFirst());
        assertEquals(2, result.get(0).getSecond().getFirst());
        assertEquals(200.0, result.get(0).getSecond().getSecond());
    }

    @Test
    void getAllProductsByUserId_ShouldReturnEmptyList_WhenNoInCartProducts() {
        // Arrange
        Long userId = 1L;
        List<Cart> carts = new ArrayList<>();

        Cart orderedCart = new Cart(testUser, testProduct);
        orderedCart.setStatus(CartStatus.IN_ORDER);
        carts.add(orderedCart);

        when(cartRepository.findAllByUserId(userId)).thenReturn(carts);

        // Act
        List<Pair<Product, Pair<Integer, Double>>> result =
                cartService.getAllProductsByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void increaseQuantityByProductId_ShouldIncreaseQuantityAndAmount() {
        // Arrange
        Long productId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUserAndProductAndStatus(
                testUser, testProduct, CartStatus.IN_CART))
                .thenReturn(testCart);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        int initialQuantity = testCart.getProductQuantity();
        double initialAmount = testCart.getAmount();

        // Act
        boolean result = cartService.increaseQuantityByProductId(productId);

        // Assert
        assertTrue(result);
        assertEquals(initialQuantity + 1, testCart.getProductQuantity());
        assertEquals(initialAmount + testProduct.getCost(), testCart.getAmount());
        verify(cartRepository).save(testCart);
    }

    @Test
    void removeCartByProductId_ShouldDeleteCart() {
        // Arrange
        Long productId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUserAndProductAndStatus(
                testUser, testProduct, CartStatus.IN_CART))
                .thenReturn(testCart);
        doNothing().when(cartRepository).delete(testCart);

        // Act
        boolean result = cartService.removeCartByProductId(productId);

        // Assert
        assertTrue(result);
        verify(cartRepository).delete(testCart);
    }

    @Test
    void removeCartByProductId_ShouldCallDeleteWithCorrectCart() {
        // Arrange
        Long productId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUserAndProductAndStatus(
                testUser, testProduct, CartStatus.IN_CART))
                .thenReturn(testCart);

        // Act
        cartService.removeCartByProductId(productId);

        // Assert
        verify(cartRepository).delete(testCart);
        verify(userService).getCurrentUser();
        verify(productService).getProductById(productId);
    }

    @Test
    void saveCart_ShouldCreateCartWithCorrectInitialValues() {
        // Arrange
        Long productId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductById(productId)).thenReturn(testProduct);
        when(cartRepository.findAllByUserAndProduct(testUser, testProduct))
                .thenReturn(Collections.emptyList());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        boolean result = cartService.saveCart(productId);

        // Assert
        assertTrue(result);
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertEquals(testUser, savedCart.getUser());
        assertEquals(testProduct, savedCart.getProduct());
        assertEquals(1, savedCart.getProductQuantity());
        assertEquals(testProduct.getCost(), savedCart.getAmount());
        assertEquals(CartStatus.IN_CART, savedCart.getStatus());
    }
}
