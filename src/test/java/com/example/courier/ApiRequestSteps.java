package com.example.courier;

import com.example.courier.model.Driver;
import com.example.courier.service.DriverService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ApiRequestSteps{

    private String jsonPayload;
    private Response response;
    private String token;




    @Given("my json:")
    public void iHaveTheFollowingJSONPayload(String json) {
        this.jsonPayload = json;
    }

    @When("I send a POST request to {string}")
    public void iSendAPostRequestTo(String endpoint) {
        this.response = given()
                .contentType("application/json")
                .body(jsonPayload)
                .when()
                .post(endpoint);

        String responseBody = response.getBody().asString();
        token = responseBody;
        assertEquals("Статус не соответствует", 200, response.statusCode());
        assertThat(response.statusCode(), is(200));
    }

    @When("I delete a test Driver")
    public void deleteTestDriver(){
        this.response = given()
                .contentType("application/json")
                .header("Authorization",  token)
                .when()
                .post("/app/delete_by_token");
        System.out.println(response.getBody().asString());
        assertEquals("Статус не соответствует", 200, response.statusCode());
    }

}