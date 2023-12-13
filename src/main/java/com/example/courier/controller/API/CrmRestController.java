package com.example.courier.controller.API;

import com.example.courier.model.exception.DriversIsEmptyException;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.NoSuchElementException;

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
    @ApiResponse(responseCode = "302", description = "Не коррекктная работа с полем delivery")
    @PostMapping("/create")
    public ResponseEntity<String> create(
            @RequestBody String json,
            @RequestHeader("Authorization") String token) throws JsonProcessingException {
        try {
            securityService.crmSecurity(token);
            orderService.startTimerSum();
            orderService.newOrder(json);
            return ResponseEntity.ok("ok");
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).body("Invalid token");
        } catch (ParseException e) {
            return ResponseEntity.status(302).body("Не коррекктная работа с полем delivery");
        }
    }

    @Operation(summary = "удалить заказ", description =  """
                                        Input: {
                                        "guid": "guid"
                                        } Output:{ok}""")
    @ApiResponse(responseCode = "200", description = "Успешно")
    @ApiResponse(responseCode = "404", description = "Нет заказа с таким номером")
    @ApiResponse(responseCode = "403", description = "Ошибка доступа")
    @PostMapping("/delete")
    public ResponseEntity<String> delete(
            @RequestBody String json,
            @RequestHeader("Authorization") String token) throws JsonProcessingException {
        try {
            securityService.crmSecurity(token);
            orderService.delete(json);
            return ResponseEntity.ok("ok");
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).body("Invalid token");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Нет заказа с таким номером");
        }
    }

    @Operation(summary = "Дай гуид водителя по гуиду заказа", description =  """
                                        Input: {
                                        "guid": "guid"
                                        } Output:{guid}""")
    @ApiResponse(responseCode = "200", description = "Успешно")
    @ApiResponse(responseCode = "404", description = "Нет заказа с таким номером")
    @ApiResponse(responseCode = "403", description = "Ошибка доступа")
    @PostMapping("/who_is_driver")
    public ResponseEntity<String> whoIsDriver(
            @RequestBody String json,
            @RequestHeader("Authorization") String token) {
        try {
            securityService.crmSecurity(token);
            return ResponseEntity.ok(orderService.whoIsDriver(json));
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).body("Invalid token");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Нет заказа с таким номером");
        } catch (DriversIsEmptyException e) {
            return ResponseEntity.status(406).body("На этот заказ не назначен водитель");
        }
    }

}
