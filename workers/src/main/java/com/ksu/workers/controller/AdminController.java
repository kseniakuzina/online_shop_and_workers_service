package com.ksu.workers.controller;

import com.ksu.workers.entities.Role;
import com.ksu.workers.entities.User;
import com.ksu.workers.service.TaskService;
import com.ksu.workers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("success/admin")
public class AdminController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String adminPage(Model model) {
        return "admin";
    }

    @GetMapping("/allusers")
    public String getAllUsers(Model model) {
        List<User> users = userService.allUsers();
        model.addAttribute("users", users);
        for (User i:users) System.out.println(i.getUsername());
        return "allusers";
    }

    @GetMapping("/allroles")
    public String getAllRoles(Model model) {
        List<Role> roles = userService.allRoles();
        model.addAttribute("roles", roles);
        for (Role i:roles) System.out.println(i.getName());
        return "allroles";
    }

    @GetMapping("/grantrole")
    public String grantRole(Model model) {
        return "grantrole";
    }

    @PostMapping("/grantrole")
    public String grantRole(String username, String name) {
        userService.grantRole(username,name);
        return "redirect:/success/admin";
    }
    @GetMapping("/addrole")
    public String addRole(Model model) {
        return "addrole";
    }

    @PostMapping("/addrole")
    public String saveRole(String name) {
        userService.saveRole(name);
        return "redirect:/success/admin";
    }

    @GetMapping("/alluserswithroles")
    public String getAllUsersWithRoles(Model model) {
        List<Pair<String,String>> usernamesWithRoles = userService.allUsersWithRoles();
        model.addAttribute("usernameswithroles", usernamesWithRoles);
        return "alluserswithroles";
    }
}
