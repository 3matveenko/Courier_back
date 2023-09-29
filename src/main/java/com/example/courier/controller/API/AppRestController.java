package com.example.courier.controller.API;

import com.example.courier.model.exception.AuthoryException;
import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.service.DriverService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "app", description = "API для запросов от мобильных приложений")
@RestController
@RequestMapping("/app")
public class AppRestController {

    @Autowired
    DriverService driverService;
    @Operation(summary = "регистрация", description =  """
                                        Input: {
                                        "login": "логин",
                                        "password": "пароль",
                                        "name": "пароль",
                                        } Output:{token}""")
    @ApiResponse(responseCode = "200", description = "Успешная регистрация")
    @ApiResponse(responseCode = "400", description = "Ошибка json")
    @ApiResponse(responseCode = "505", description = "Такой логин существует")
    @PostMapping("/create")
    public ResponseEntity<String> create(
            @RequestBody String json) throws JsonProcessingException {
        try {
            return driverService.create(json);
        } catch (AuthoryException e){
            return ResponseEntity.status(505).body("Invalid login");
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        }

    }

    @Operation(summary = "авторизация", description = """
                                        Input: {
                                          "login": "логин",
                                          "password": "пароль"} 
                                          Output:200""")
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    @ApiResponse(responseCode = "400", description = "Ошибка json")
    @ApiResponse(responseCode = "403", description = "Такой логин не существует или не верный пароль")
    @PostMapping("/authorization")
    public ResponseEntity<String> authorization(
            @RequestBody String json) {
        try {
            return driverService.authorization(json);
        } catch (ForbiddenException e){
            return ResponseEntity.status(403).body("Invalid authorization");
        } catch (JsonProcessingException e){
            return ResponseEntity.status(400).body("Bad request");
        }

    }


}



