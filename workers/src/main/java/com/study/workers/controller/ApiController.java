package com.study.workers.controller;

import com.study.common.entities.OrderDTO;
import com.study.common.entities.ProductDTOWithId;
import com.study.workers.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

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