package com.ksu.workers.controller;

import com.ksu.workers.service.TaskService;
import com.ksu.workers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("success/storage")
public class StorageController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String storagePage(Model model) {
        return "storage";
    }

}