package com.example.courier;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ApiRequestSteps{

    private String jsonPayload;
    private Response response;

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
        assertEquals("Статус не соответствует", 200, response.statusCode());
        assertThat(response.statusCode(), is(200));
    }
}