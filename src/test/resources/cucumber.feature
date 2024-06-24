Feature: cucumber
    Scenario: Successful login with valid credentials
    Given my json:
            """
            {
                "login": "1",
                "password": "1",
                "name": "1"
            }
            """
    When I send a POST request to "/app/create"
        Given my json:
                """
                {
                    "login": "1",
                    "password": "1"
                }
                """
        When I send a POST request to "/app/authorization"



