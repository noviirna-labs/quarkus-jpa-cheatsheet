package io.github.noviirna.resource.panacheentity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.noviirna.entity.BaseEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

// src/test/java/base/PanacheRestIntegrationTest.java
@QuarkusTest
public abstract class RestPanacheTest<T extends BaseEntity> {

    protected abstract RestPanacheTestFactory<T> factory();

    @BeforeEach
    public void prepare() {
        factory().purgeEntity();
    }


    private static final long VALUE_ID = 1l;
    private static final String FIELDNAME_ID = "id";

    private static final String PATHPARAM_ID = "/{id}";
    private static final String PATH_COUNT = "/count";


    @Test
    protected void testCount_whileApiIsDisabled_405() throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(factory().insertEntity()))
                .when()
                .post(factory().getPath() + PATH_COUNT)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    protected void testFindById_isExists_200() throws JsonProcessingException {
        BaseEntity p = factory().insertEntity();
        given()
                .when()
                .pathParam(FIELDNAME_ID, p.id.intValue())
                .get(factory().getPath() + PATHPARAM_ID)
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(
                        FIELDNAME_ID, Matchers.notNullValue(),
                        FIELDNAME_ID, Matchers.is(p.id.intValue())
                )
        ;
    }

    @Test
    protected void testFindById_isNotFound_404() {
        given()
                .when()
                .pathParam(FIELDNAME_ID, VALUE_ID)
                .get(factory().getPath() + PATHPARAM_ID)
                .then().assertThat()
                .statusCode(RestResponse.StatusCode.NOT_FOUND);
    }


    @Test
    protected void testFindAll_returnList_200() throws JsonProcessingException {
        BaseEntity p = factory().insertEntity();
        given()
                .when()
                .get(factory().getPath())
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("$", Matchers.hasSize(1))
        ;
    }

    @Test
    protected void testFindAll_returnEmpty_200() {
        given()
                .when()
                .get(factory().getPath())
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.OK);
    }

    @Test
    protected void testUpdate_whileNoRecords_500() throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_ID, VALUE_ID)
                .body(new ObjectMapper().writeValueAsString(factory().insertEntity()))
                .when()
                .put(factory().getPath() + PATHPARAM_ID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    protected void testUpdate_success_204() throws JsonProcessingException {
        BaseEntity p = factory().insertEntity();
        ObjectMapper om = new ObjectMapper();
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_ID, p.id)
                .body(om.writeValueAsString(p))
                .when()
                .put(factory().getPath() + PATHPARAM_ID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.NO_CONTENT);
    }


    @Test
    protected void testHardDelete_success_204() {
        BaseEntity p = factory().insertEntity();

        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_ID, p.id)
                .when()
                .delete(factory().getPath() + PATHPARAM_ID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.NO_CONTENT);
    }

    @Test
    protected void testHardDelete_isNotFound_404() {
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_ID, VALUE_ID)
                .when()
                .delete(factory().getPath() + PATHPARAM_ID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.NOT_FOUND);
    }


    @Test
    protected void testPost_whileApiIsDisabled_405() throws JsonProcessingException {
        BaseEntity p = factory().insertEntity();
        ObjectMapper om = new ObjectMapper();
        given()
                .contentType(ContentType.JSON)
                .body(om.writeValueAsString(p))
                .when()
                .post(factory().getPath())
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.METHOD_NOT_ALLOWED);
    }
}