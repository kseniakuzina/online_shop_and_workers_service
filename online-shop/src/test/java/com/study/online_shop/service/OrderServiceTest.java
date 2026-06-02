package com.study.online_shop.service;

import com.study.common.entities.*;
import com.study.online_shop.entities.*;
import com.study.online_shop.repository.CartRepository;
import com.study.online_shop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private Order testOrder;
    private HttpHeaders testHeaders;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "workersServiceForOrderUrl", "http://localhost:8082/api/orders");
        ReflectionTestUtils.setField(orderService, "workersServiceForStorageUrl", "http://localhost:8082/api/storage");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setCost(100.0);
        testProduct.setQuantity(10);

        testCart = new Cart(testUser, testProduct);
        testCart.setId(1L);
        testCart.setProductQuantity(2);
        testCart.setAmount(200.0);
        testCart.setStatus(CartStatus.IN_CART);

        testOrder = new Order();
        testOrder.setId(1L);

        testHeaders = new HttpHeaders();
        testHeaders.add("Authorization", "Bearer token");
    }

    @Test
    void moveCartToOrder_ShouldCreateOrderAndReturnTrue_WhenStockIsSufficient() {
        // Arrange
        String address = "Test Address";
        List<Cart> carts = Collections.singletonList(testCart);
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findAllByUserIdAndStatus(testUser.getId(), CartStatus.IN_CART)).thenReturn(carts);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(restTemplate.postForEntity(eq("http://localhost:8082/api/orders"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        // Act
        boolean result = orderService.moveCartToOrder(address, testHeaders);

        // Assert
        assertTrue(result);
        assertEquals(CartStatus.IN_ORDER, testCart.getStatus());
        assertEquals(8, testProduct.getQuantity());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(testCart);
    }

    @Test
    void moveCartToOrder_ShouldReturnFalse_WhenStockIsInsufficient() {
        // Arrange
        String address = "Test Address";
        testProduct.setQuantity(0);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findAllByUserIdAndStatus(testUser.getId(), CartStatus.IN_CART))
                .thenReturn(Collections.singletonList(testCart));

        // Act
        boolean result = orderService.moveCartToOrder(address, testHeaders);

        // Assert
        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void moveCartToOrder_ShouldSendProductToStorage_WhenQuantityBecomesZero() {
        // Arrange
        String address = "Test Address";
        testProduct.setQuantity(2); // Same as cart quantity
        List<Cart> carts = Collections.singletonList(testCart);
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setCarts(carts);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findAllByUserIdAndStatus(testUser.getId(), CartStatus.IN_CART)).thenReturn(carts);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(restTemplate.postForEntity(eq("http://localhost:8082/api/orders"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));
        when(restTemplate.postForEntity(eq("http://localhost:8082/api/storage"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        // Act
        boolean result = orderService.moveCartToOrder(address, testHeaders);

        // Assert
        assertTrue(result);
        assertEquals(0, testProduct.getQuantity());
        verify(restTemplate).postForEntity(eq("http://localhost:8082/api/storage"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void moveCartToOrder_ShouldReturnFalse_WhenRestTemplateThrowsException() {
        // Arrange
        String address = "Test Address";
        List<Cart> carts = Collections.singletonList(testCart);
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findAllByUserIdAndStatus(testUser.getId(), CartStatus.IN_CART)).thenReturn(carts);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(restTemplate.postForEntity(eq("http://localhost:8082/api/orders"), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // Act
        boolean result = orderService.moveCartToOrder(address, testHeaders);

        // Assert
        assertFalse(result);
    }

    @Test
    void moveCartToOrder_ShouldProcessMultipleCarts() {
        // Arrange
        String address = "Test Address";
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setCost(200.0);
        product2.setQuantity(5);

        Cart cart2 = new Cart(testUser, product2);
        cart2.setId(2L);
        cart2.setProductQuantity(3);
        cart2.setAmount(600.0);
        cart2.setStatus(CartStatus.IN_CART);

        List<Cart> carts = Arrays.asList(testCart, cart2);
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findAllByUserIdAndStatus(testUser.getId(), CartStatus.IN_CART)).thenReturn(carts);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(restTemplate.postForEntity(eq("http://localhost:8082/api/orders"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        // Act
        boolean result = orderService.moveCartToOrder(address, testHeaders);

        // Assert
        assertTrue(result);
        assertEquals(CartStatus.IN_ORDER, testCart.getStatus());
        assertEquals(CartStatus.IN_ORDER, cart2.getStatus());
        assertEquals(8, testProduct.getQuantity());
        assertEquals(2, product2.getQuantity());
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void getAllOrdersByUser_ShouldReturnUserOrders() {
        // Arrange
        List<Order> expectedOrders = Collections.singletonList(testOrder);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findAllByUser(testUser)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getAllOrdersByUser();

        // Assert
        assertEquals(expectedOrders, result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOrdersByUser_ShouldReturnEmptyList_WhenNoOrders() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findAllByUser(testUser)).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getAllOrdersByUser();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() {
        // Arrange
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.ACCEPTED;
        Pair<Long, OrderStatus> pair = Pair.of(orderId, newStatus);

        when(orderRepository.getReferenceById(orderId)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.updateOrderStatus(pair);

        // Assert
        assertEquals(newStatus, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void moveCartToOrder_ShouldSetOrderReferenceOnCart() {
        // Arrange
        String address = "Test Address";
        List<Cart> carts = Collections.singletonList(testCart);
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findAllByUserIdAndStatus(testUser.getId(), CartStatus.IN_CART)).thenReturn(carts);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(restTemplate.postForEntity(eq("http://localhost:8082/api/orders"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        // Act
        orderService.moveCartToOrder(address, testHeaders);

        // Assert
        assertEquals(savedOrder, testCart.getOrder());
    }
}