package com.example.courier.controller;

import com.example.courier.model.Setting;
import com.example.courier.model.User;
import com.example.courier.model.data.AllSettings;
import com.example.courier.service.DriverService;
import com.example.courier.service.OrderService;
import com.example.courier.service.SettingService;
import com.example.courier.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Controller
public class MainController {

    @Autowired
    UserService userService;

    @Autowired
    OrderService orderService;

    @Autowired
    DriverService driverService;

    @Autowired
    SettingService settingService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/")
    public String index(Model model,
                        HttpSession session){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = (User) userService.loadUserByUsername(email);
        session.setAttribute("userID", user.getId());
        session.setAttribute("userNAME", user.getName());
        model.addAttribute("drivers", driverService.getAll());
        model.addAttribute("orders", orderService.getOrderItWork());
        return "map/map";
    }

    @GetMapping(value = "/login")
    public String login(){
        return "login/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/orders")
    public String orders(
            Model model){
        Date date = new Date();
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        model.addAttribute("date", date);
        model.addAttribute("orders", orderService.getOrderByDate(date));
        return "order/orders";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/orders_date")
    public String ordersDate(
            Model model,
            @RequestParam("date") String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);
        model.addAttribute("date", date);
        model.addAttribute("orders", orderService.getOrderByDate(date));
    return "order/orders";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/change_status/{driverId}")
    public String changeStatus(@PathVariable Long driverId){
        driverService.changeDriverOrderStatus(driverId);
        return "redirect:/";
    }

    @GetMapping(value = "/delete_driver/{driverId}")
    public String deleteDriver(
            @PathVariable Long driverId){
        driverService.deleteById(driverId);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/settings")
    public String settings(Model model){
        model.addAttribute("allSettings", settingService.collectingSettings());
        return "settings/settings";
    }
}
