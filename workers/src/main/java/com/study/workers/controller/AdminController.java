package com.study.workers.controller;

import com.study.workers.entities.GrantRoleStatus;
import com.study.workers.entities.Role;
import com.study.workers.entities.User;
import com.study.workers.service.TaskService;
import com.study.workers.service.UserService;
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
    public String grantRole(String username, String name, Model model) {
        GrantRoleStatus status = userService.grantRole(username, name);

        switch (status) {
            case GRANT_SUCCESS ->
                    model.addAttribute("success", "Роль '" + name + "' успешно выдана пользователю " + username);

            case USER_NOT_FOUND ->
                    model.addAttribute("error", "Ошибка: пользователь '" + username + "' не найден");

            case ROLE_NOT_FOUND ->
                    model.addAttribute("error", "Ошибка: роль '" + name + "' не найдена");

            case USER_ROLE_ALREADY_EXISTS ->
                    model.addAttribute("error", "Ошибка: пользователю '" + username + "' уже выдана роль '" + name + "'");

            case GRANT_FAILURE ->
                    model.addAttribute("error", "Ошибка: не удалось выдать роль '" + name + "' пользователю " + username);

            default ->
                    model.addAttribute("error", "Неизвестная ошибка");
        }

        return "grantrole";
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
