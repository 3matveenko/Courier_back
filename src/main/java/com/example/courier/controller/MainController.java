package com.example.courier.controller;

import com.example.courier.model.Setting;
import com.example.courier.model.User;
import com.example.courier.model.data.AllSettings;
import com.example.courier.model.exception.AuthoryException;
import com.example.courier.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    @Autowired
    AssignService assignService;

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
        Date date = new  Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli());
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        model.addAttribute("date", date);
        model.addAttribute("orders", orderService.getOrderByDate(date));
        return "order/orders";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/assigns")
    public String assigns(
            Model model){
        Date date = new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli());
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        model.addAttribute("date", date);
        model.addAttribute("assigns", assignService.getAssignsByDate(date));
        return "assign/assigns";
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
    @PostMapping(value = "/assigns_date")
    public String assignsDate(
            Model model,
            @RequestParam("date") String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);
        model.addAttribute("date", date);
        model.addAttribute("assigns", assignService.getAssignsByDate(date));
        return "assign/assigns";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/change_status/{driverId}")
    public String changeStatus(@PathVariable Long driverId) throws AuthoryException {
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/save_settings")
    public String savingSettings(
            @ModelAttribute AllSettings allSettings,
            Model model){
        settingService.updatingSettings(allSettings);
        model.addAttribute("allSettings", settingService.collectingSettings());
        model.addAttribute("flagSave",true);
        return "settings/settings";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/change_driver")
    public String changeDriver(
            @RequestParam String driverToken,
            @RequestParam Long orderId
    ){
        orderService.giveTheDriverAnOrder(driverToken,orderId);
        return "redirect:/";
    }
}
