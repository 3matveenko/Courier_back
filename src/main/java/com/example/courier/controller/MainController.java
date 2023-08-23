package com.example.courier.controller;

import com.example.courier.model.User;
import com.example.courier.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/")
    public String index(Model model,
                        HttpSession session){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = (User) userService.loadUserByUsername(email);
        session.setAttribute("userID", user.getId());
        session.setAttribute("userNAME", user.getName());
        return "map/map";
    }

    @GetMapping(value = "/login")
    public String login(){
        return "login/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/orders")
    public String orders(){
        return "order/orders";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/drivers")
    public String drivers(){
        return "driver/drivers";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/settings")
    public String settings(){
        return "settings/settings";
    }
}
