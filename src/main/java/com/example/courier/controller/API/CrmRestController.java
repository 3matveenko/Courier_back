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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

            orderService.startTimerSum();
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
//гет запрос
    public static void sendGetRequestToGoogle() {
        try {
            URL url = new URL("http://www.google.com");

            // Открываем соединение (connection) с URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса как GET
            connection.setRequestMethod("GET");

            // Получаем ответ от сервера
            int responseCode = connection.getResponseCode();

            // Проверяем, что ответ успешный (код 200)
            if (responseCode == 200) {
                // Чтение данных из ответа
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();

                // Выводим полученные данные
                System.out.println(response.toString());
            } else {
                System.out.println("GET-запрос не выполнен. Код ответа: " + responseCode);
            }

            connection.disconnect(); // Закрываем соединение
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
