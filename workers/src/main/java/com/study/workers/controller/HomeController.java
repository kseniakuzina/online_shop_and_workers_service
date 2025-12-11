package com.study.workers.controller;

import com.study.workers.entities.Task;
import com.study.workers.entities.User;
import com.study.workers.service.TaskService;
import com.study.workers.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/success")
public class HomeController {
    private UserService userService;
    private TaskService taskService;

    public HomeController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping
    public String success(Model model) {
        String username = userService.getCurrentUsername();
        var tr = userService.getCurrentUser().getAuthorities();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : tr) {
            System.out.println(authority.getAuthority());
            roles.add(authority.getAuthority());
        }
        model.addAttribute("username", username);
        model.addAttribute("roles", roles);
        return "success";
    }


    @GetMapping("/tasks")
    public String taskList(Model model){
        List<Task> tasks = taskService.findActiveTasksByUserId(userService.getUserIdByUsername(userService.getCurrentUsername()));
        model.addAttribute("tasks", tasks);
        return "tasks";
    }


    @PostMapping("/tasks/complete")
    public String taskCompleted(@RequestParam Long id, HttpServletRequest request){
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("service-account", "secure-password");
        if (csrfToken != null) {
            System.out.println("туть");
            headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
        }
        taskService.completeTask(id, headers);
        return "redirect:/success/tasks";
    }

    @GetMapping("/account")
    public String account(Model model){
        User currUser = userService.getCurrentUser();
        model.addAttribute("currUser",currUser);
        return "account";
    }

    @PostMapping("/account/update")
    public String updateAccount(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String phone) {
        userService.updateUser(firstName,lastName,username,email,phone);
        return "redirect:/success";
    }
}
