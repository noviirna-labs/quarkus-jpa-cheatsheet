package io.github.noviirna.resource.panacheentity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.noviirna.entity.BaseEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public abstract class RestPanacheTest<T extends BaseEntity> {

    protected abstract RestPanacheTestFactory<T> factory();

    @Inject
    protected ObjectMapper objectMapper; // This instance respects quarkus.jackson properties

    @BeforeEach
    public void prepare() {
        factory().purgeEntity();
    }

    protected static final long VALUE_ID = 1L;
    protected static final String FIELDNAME_ID = "id";

    protected static final String PATHPARAM_ID = "/{id}";
    protected static final String PATH_COUNT = "/count";

    @Test
    protected void testCount_whileApiIsDisabled_405() throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(factory().insertEntity()))
                .when()
                .post(factory().getPath() + PATH_COUNT)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    protected void testFindById_isExists_200() {
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
    protected void testFindAll_returnList_200() {
        factory().insertEntity();
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
                .body(objectMapper.writeValueAsString(factory().insertEntity()))
                .when()
                .put(factory().getPath() + PATHPARAM_ID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    protected void testUpdate_success_204() throws JsonProcessingException {
        BaseEntity p = factory().insertEntity();
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_ID, p.id)
                .body(objectMapper.writeValueAsString(p))
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
    protected void testPost_isSuccess_201() throws JsonProcessingException {
        T p = factory().insertEntity();
        factory().delete(p);
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(p))
                .when()
                .post(factory().getPath())
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.CREATED);
    }

    @Test
    protected void testPost_invalidParameter_400() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(factory().getPath())
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.BAD_REQUEST);
    }

}