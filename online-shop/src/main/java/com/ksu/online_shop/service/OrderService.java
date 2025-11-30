package com.ksu.online_shop.service;

import com.ksu.common.entities.*;
import com.ksu.online_shop.entities.*;
import com.ksu.online_shop.repository.CartRepository;
import com.ksu.online_shop.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class OrderService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    UserService userService;

    String workersServiceForOrderUrl = "http://localhost:8082/api/orders";
    String workersServiceForStorageUrl = "http://localhost:8082/api/storage";

    public boolean moveCartToOrder(String address, HttpHeaders headers) {
        User user = userService.getCurrentUser();
        List<Cart> carts = cartRepository.findAllByUserIdAndStatus(user.getId(), CartStatus.IN_CART);
        if (carts.stream().allMatch(cart -> cart.getProductQuantity() <= cart.getProduct().getQuantity())){
            Order order = new Order(user, carts, address);
            Order savedOrder = orderRepository.save(order);
            System.out.println(savedOrder.getId());
            for (Cart cart: carts){
                cart.setOrder(savedOrder);
                cart.setStatus(CartStatus.IN_ORDER);
                int prevQuantity = cart.getProduct().getQuantity();
                cart.getProduct().setQuantity(prevQuantity-cart.getProductQuantity());
                if (cart.getProduct().getQuantity()==0){
                    ProductDTOWithId productDTO = mapProduct(cart.getProduct());
                    HttpEntity<ProductDTOWithId> productRequest = new HttpEntity<>(productDTO, headers);
                    restTemplate.postForEntity(workersServiceForStorageUrl,productRequest, String.class);
                }
                cartRepository.save(cart);
            }
            OrderDTO dto = mapOrder(savedOrder);
            System.out.println(dto.getId());
            HttpEntity<OrderDTO> orderRequest = new HttpEntity<>(dto, headers);
            System.out.println(orderRequest.getBody().getCarts().size());
            try {
                restTemplate.postForEntity(workersServiceForOrderUrl, orderRequest, String.class);
            } catch (Exception e) {
                log.error("Error sending order to workers service: {}", e.getMessage());
                return false;
            }
            return true;
        }
        else{
            return false;
        }
    }

    public List<Order> getAllOrdersByUser(){
        User user = userService.getCurrentUser();
        return orderRepository.findAllByUser(user);
    }

    public void updateOrderStatus(Pair<Long, OrderStatus> orderIdAndStatus){
        System.out.println("меняююю");
        Order order = orderRepository.getReferenceById(orderIdAndStatus.getFirst());
        order.setStatus(orderIdAndStatus.getSecond());
        orderRepository.save(order);
    }

    private OrderDTO mapOrder(Order order){
        var carts = order.getCarts();
        List<CartDTO> cartsDTO = new ArrayList<>();
        for (Cart cart: carts){
            Product product = cart.getProduct();
            ProductDTOWithId productDTO = mapProduct(product);
            cartsDTO.add(new CartDTO(cart.getId(),productDTO,cart.getProductQuantity(), cart.getAmount(),cart.getStatus()));
        }
        return new OrderDTO(order.getId(), order.getQuantity(), order.getAmount(),
                cartsDTO,order.getStatus(), order.getAddress());
    }

    private ProductDTOWithId mapProduct(Product product){
        return new ProductDTOWithId(product.getId(),product.getName(), product.getQuantity(), product.getMaterial(), product.getCost());
    }
}