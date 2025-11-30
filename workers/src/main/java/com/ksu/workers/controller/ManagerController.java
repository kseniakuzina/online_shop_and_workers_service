package com.ksu.workers.controller;

import com.ksu.common.entities.OrderDTO;
import com.ksu.common.entities.ProductDTO;
import com.ksu.common.entities.ProductDTOWithId;
import com.ksu.workers.DTO.TaskDTO;
import com.ksu.workers.entities.Role;
import com.ksu.workers.entities.Task;
import com.ksu.workers.entities.User;
import com.ksu.workers.service.TaskService;
import com.ksu.workers.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("success/manager")
public class ManagerController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String managerPage(Model model) {
        return "manager";
    }

    @PostMapping
    public String saveTask(TaskDTO dto) {
        taskService.saveTask(dto);
        return "redirect:/success/manager";
    }
    @GetMapping("/order")
    public String orderPage(Model model) {
        List<Task> tasks = taskService.findAllTasks();
        model.addAttribute("tasks",tasks);
        return "order";
    }

}

