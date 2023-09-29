package com.example.courier.controller.API;

import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.service.LocationService;
import com.example.courier.service.OrderService;
import com.example.courier.service.SecurityService;
import com.example.courier.service.SettingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "crm", description = "API для запросов от 1c")
@RestController
@RequestMapping("/crm")
public class CrmRestController {


    @Autowired
    OrderService orderService;

    @Autowired
    SecurityService securityService;

    @Autowired
    SettingService settingService;

    @Autowired
    LocationService locationService;

    @Operation(summary = "новый заказ", description =  """
                                        Input: {
                                        "guid": "guid",
                                        "address": "адрес доставки",
                                        "phone": "номер телефона",
                                        "current": "имя клиента",
                                        "latitude": "широта",
                                        "longitude": "долгота",
                                        "driver_token": "токен водителя",(driver_token="", значит водитель не назначен)
                                        } Output:{пока вопрос с ответом. надо понять как ответит водитель}""")
    @ApiResponse(responseCode = "200", description = "Успешно")
    @ApiResponse(responseCode = "400", description = "Ошибка json")
    @ApiResponse(responseCode = "403", description = "Ошибка доступа")
    @PostMapping("/create")
    public ResponseEntity<String> create(
            @RequestBody String json,
            @RequestHeader("Authorization") String token) throws JsonProcessingException {
        try {
System.out.println("итоговый угол = "+locationService.angleBetweenVerticalAndPoint(43.244487157876925, 76.88878579396281));
            orderService.newTimer(1);//добавить проверку на то существует таймер или нет
            securityService.crmSecurity(token);
            orderService.newOrder(json);
            return ResponseEntity.ok("ok");
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).body("Invalid token");
        }

    }

    @GetMapping("/old")
    public ResponseEntity<String> get(
            @RequestHeader("Authorization") String headerValue){
        if ("Bearer qwerty".equals(headerValue)){
            System.out.println(headerValue);
            return ResponseEntity.ok("Ok");
        } else {
            System.out.println(headerValue);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
    }

}
