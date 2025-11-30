package com.ksu.workers.controller;

import com.ksu.common.entities.OrderDTO;
import com.ksu.common.entities.ProductDTOWithId;
import com.ksu.workers.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ManagerApiController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/orders")
    public ResponseEntity<?> newOrder(@RequestBody OrderDTO order) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("service-account", "secure-password");

        taskService.newOrder(order, headers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/storage")
    public ResponseEntity<?> newStorageRequest(@RequestBody ProductDTOWithId product) {
        taskService.newStorageRequest(product);
        return ResponseEntity.ok().build();
    }

}