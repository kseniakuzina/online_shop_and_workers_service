package com.study.online_shop.controller;

import com.study.common.entities.ClothesType;
import com.study.common.entities.Gender;
import com.study.online_shop.entities.Product;
import com.study.online_shop.entities.User;
import com.study.online_shop.service.ProductService;
import com.study.online_shop.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/home")
public class HomeController {
    private UserService userService;
    private ProductService productService;

    public HomeController(UserService userService, ProductService productService){
        this.userService = userService;
        this.productService = productService;
    }

    @ModelAttribute("genderMap")
    public Map<String, String> getGenderMap() {
        Map<String, String> genderMap = new HashMap<>();
        genderMap.put("MALE", "Мужской");
        genderMap.put("FEMALE", "Женский");
        return genderMap;
    }

    @ModelAttribute("clothesTypeMap")
    public Map<String, String> getClothesTypeMap() {
        Map<String, String> clothesMap = new HashMap<>();
        clothesMap.put("OUTERWEAR", "Верхняя одежда");
        clothesMap.put("TOP", "Верх");
        clothesMap.put("BOTTOM", "Низ");
        clothesMap.put("DRESS", "Платья");
        clothesMap.put("FOOTWEAR", "Обувь");
        clothesMap.put("HEADWEAR", "Головные уборы");
        clothesMap.put("ACCESSORIES", "Аксессуары");
        return clothesMap;
    }

    @GetMapping
    public String home(Model model,
                       @RequestParam(required = false) String gender,
                       @RequestParam(required = false) String category) {

        String username = userService.getCurrentUsername();
        var authorities = userService.getCurrentUser().getAuthorities();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            roles.add(authority.getAuthority());
        }
        List<Product> products;
        if ((gender == null || gender.isEmpty()) && (category == null || category.isEmpty())) {
            products = productService.getAllProductsWhereQuantityGreaterThenZero();
        } else {
            products = productService.getFilteredProducts(gender,category);
        }

        model.addAttribute("currentGender", gender);
        model.addAttribute("currentCategory", category);
        model.addAttribute("username", username);
        model.addAttribute("roles", roles);
        model.addAttribute("products", products);

        return "home";
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
        return "redirect:/home/account";
    }

}
