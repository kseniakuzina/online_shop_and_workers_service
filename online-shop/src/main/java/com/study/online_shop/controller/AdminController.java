package com.study.online_shop.controller;

import com.study.common.entities.ProductDTO;
import com.study.online_shop.entities.User;
import com.study.online_shop.service.ProductService;
import com.study.online_shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/home/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

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

    @GetMapping("/newproduct")
    public String newProduct(Model model) {
        return "newproduct";
    }

    @PostMapping("/newproduct")
    public String addNewProduct(ProductDTO dto ,
                                @RequestParam("imageFile") MultipartFile imageFile)
            throws IOException {
        System.out.println("here1");
        if (imageFile != null && !imageFile.isEmpty()) {
            // Генерируем уникальное имя файла
            String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();

            // Путь к папке uploads/images
            Path uploadPath = Paths.get("C:/project_uploads/images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // создаем, если нет
            }

            // Сохраняем файл
            Path filePath = uploadPath.resolve(filename);
            imageFile.transferTo(filePath.toFile());

            // Записываем путь для отображения на сайте
            dto.setImage("/images/" + filename);
        }
        productService.saveProduct(dto);
        System.out.println("here2");
        return "redirect:/home/admin";
    }
}
