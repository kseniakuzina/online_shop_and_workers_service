package com.study.workers.controller;

import com.study.workers.DTO.TaskDTO;
import com.study.workers.entities.Task;
import com.study.workers.service.TaskService;
import com.study.workers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String saveTask(TaskDTO dto, Model model) {
        String username = taskService.saveTask(dto);
        if (username == null) {
            model.addAttribute("error", "Пользователя с таким именем не существует");
        }
        else{
            model.addAttribute("success", "Задача успешно выдана пользователю " + username);
        }
        return "manager";
    }
    @GetMapping("/order")
    public String orderPage(Model model) {
        List<Task> tasks = taskService.findAllTasks();
        model.addAttribute("tasks",tasks);
        return "order";
    }

}

