package uk.gov.hmcts.reform.locationrefdata;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class SmokeTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8099"
        );

    @Test
    public void isRunningAndHealthy() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .when()
            .get("/health")
            .then()
            .statusCode(OK.value())
            .and()
            .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }
}
