package com.example.courier.controller.API;

import com.example.courier.model.exception.DriversIsEmptyException;
import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.service.*;
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

    @Autowired
    AssignService assignService;

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
            return ResponseEntity.ok(orderService.newOrder(json));
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
    @ApiResponse(responseCode = "406", description = "На этот заказ не назначен водитель")
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

    @Operation(summary = "Дай рейсы по дате", description =  """
                                        Input: {
                                        "date": "2023-12-14"
                                        } 
                                        Output:{
                                                                                
                                                    [
                                                        {
                                                            "id": 3,
                                                            "driver": {
                                                                "id": 1,
                                                                "login": "first",
                                                                "password": "$2a$10$lL.rNu008CVXNoOJwbSlAOPwtLuuD2hCXCHXHfFdhJJMl0zFdJJ/u",
                                                                "name": "first",
                                                                "statusOrder": false,
                                                                "statusDay": true,
                                                                "token": "0d3096d9-e6e9-449d-9a34-e72bb6b38f7a",
                                                                "latitude": 43.244082,
                                                                "longitude": 76.8907413,
                                                                "timeFree": 0,
                                                                "lastUpdateLocation": 1701259951716,
                                                                "lastActivity": 1701320138744
                                                            },
                                                            "orders": [
                                                                {
                                                                    "id": 4,
                                                                    "statusDelivery": 2,
                                                                    "guid": "00000039sdv3u",
                                                                    "dateStart": 1701242423608,
                                                                    "dateEnd": 1701428691408,
                                                                    "address": "улица Ауэзова, 118 / бульвар Бухар жырау, 69 / улица Ауэзова, 118/69",
                                                                    "phone": "+77777777777",
                                                                    "current": "",
                                                                    "latitude": 43.2312,
                                                                    "longitude": 76.9054,
                                                                    "driver": null,
                                                                    "angle": 180.0,
                                                                    "rejectOrder": null,
                                                                    "comment": null,
                                                                    "sendSmS": false,
                                                                    "delivery": null,
                                                                    "driver_token": null,
                                                                    "timeStartAlmaty": 1701264023608,
                                                                    "timeEndAlmaty": 1701450291408
                                                                }
                                                            ],
                                                            "timeStart": 1701250394316,
                                                            "timeRun": null,
                                                            "timeEnd": 1701250487693,
                                                            "timeStartAlmaty": 1701271994316,
                                                            "timeRunAlmaty": 0,
                                                            "timeEndAlmaty": 1701272087693
                                                        }
                                                    ]                          
                                                }""")
    @ApiResponse(responseCode = "200", description = "Успешно")

    @PostMapping("/get_assigns_by_date")
    public ResponseEntity<String> getAssignsByDate(
            @RequestBody String json,
            @RequestHeader("Authorization") String token) {
        try {
            securityService.crmSecurity(token);
            return ResponseEntity.ok(assignService.getAssignsByStringDate(json));
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        }  catch (ForbiddenException e) {
            return ResponseEntity.status(403).body("Invalid token");
        }  catch (ParseException e) {
            return ResponseEntity.status(400).body("Не верный формат даты. ParseException");
        }
    }

}
