package com.example.courier.controller.API;

import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.service.SecurityService;
import com.example.courier.service.SettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "crm", description = "API для запросов от 1c")
@RestController
@RequestMapping("/front")
public class FrontRestController {

    @Autowired
    SecurityService securityService;

    @Autowired
    SettingService settingService;


    @GetMapping("get_time")
    public ResponseEntity<String> getTime(
            @RequestHeader("Authorization") String token) throws ForbiddenException {
        try {
            securityService.crmSecurity(token);
            return ResponseEntity.ok(settingService.getValueByKey("timer_start_time"));
        } catch (ForbiddenException e){
            return ResponseEntity.status(403).body("Invalid token");
        }

    }
}
