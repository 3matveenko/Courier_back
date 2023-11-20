package com.example.courier.service;

import com.example.courier.model.Driver;
import com.example.courier.model.data.CompleteOrder;
import com.example.courier.model.data.Message;
import com.example.courier.model.data.SendSms;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

import static com.example.courier.service.RabbitService.gson;

@Service
public class SendService {

    @Autowired
    SettingService settingService;

    @Autowired
    RabbitService rabbitService;

    public String toJson(Object object){
        try {
            return  (new ObjectMapper()).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
    public void sendTo1cAboutCreatingNewDriver(Driver driver){
        try {
            URL url = new URL(settingService.getValueByKey("crm_server_address")+"/torgupr/hs/delivery/driver");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String auth = settingService.getValueByKey("crm_login") + ":" + settingService.getValueByKey("crm_password");
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            String authHeader = "Basic " + encodedAuth;
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            String requestBody = toJson(driver);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(connection.getResponseMessage());
                // log
            } else {
                // log
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTo1cAboutCompleteOrder(String _guid, String _driverToken){
        try {
            URL url = new URL(settingService.getValueByKey("crm_server_address")+"/torgupr/hs/delivery/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String auth = settingService.getValueByKey("crm_login") + ":" + settingService.getValueByKey("crm_password");
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            String authHeader = "Basic " + encodedAuth;
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            CompleteOrder completeOrder = new CompleteOrder();
            completeOrder.setToken(_driverToken);
            completeOrder.setNumber(_guid);
            completeOrder.setStatus(true);
            String requestBody = toJson(completeOrder);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(connection.getResponseMessage());
                // log
            } else {
                // log
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSms(Message _message){
        System.out.println("SENDER получил сообщение:"+_message);
        SendSms sendSms = gson.fromJson(_message.getBody(), SendSms.class);

        try {
            // Construct URL for the GET request
            URL url = new URL("https://smsc.kz/sys/send.php?login=2KE&psw=studioevolution2018&phones="+sendSms.getPhone()+"&mes="+"Код поддтверждения: "+sendSms.getCode()+"&sender=NOVO");
            System.out.println("SENDER url :"+url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("HTTP_OK");
                Message newMessage = new Message("","send_sms_success",System.currentTimeMillis(), "");
                rabbitService.sendMessage(_message.getToken(), toJson(newMessage));
            } else {
                System.out.println("ошибка");
                System.out.println(connection.getResponseCode());
                // log error
            }

            // Disconnect after the request is complete
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
