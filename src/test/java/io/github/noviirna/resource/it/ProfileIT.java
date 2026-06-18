package io.github.noviirna.resource.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.noviirna.entity.BaseEntity;
import io.github.noviirna.entity.Profile;
import io.github.noviirna.resource.base.RestPanacheTest;
import io.github.noviirna.resource.factory.ProfileFactory;
import io.github.noviirna.resource.factory.RestPanacheTestFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ProfileIT extends RestPanacheTest<Profile> {

    @Inject
    ProfileFactory profileFactory;

    @Override
    protected RestPanacheTestFactory<Profile> factory() {
        return profileFactory;
    }


    @Override
    @Test
    protected void testHardDelete_success_204() {
        Assumptions.assumeTrue(
                false,
                "Student not support delete, skip test ini");
    }

    @Override
    @Test
    protected void testHardDelete_isNotFound_404() {
        Assumptions.assumeTrue(
                false,
                "Student not support delete, skip test ini");
    }


    @Override
    @Test
    protected void testUpdate_success_204() {
        Assumptions.assumeTrue(
                false,
                "Student not support update, skip test ini");
    }

    @Override
    @Test
    protected void testUpdate_whileNoRecords_500() {
        Assumptions.assumeTrue(
                false,
                "Student not support update, skip test ini");
    }

    @Test
    protected void testHardDelete_whileApiIsDisabled_405() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 1l)
                .when()
                .delete(factory().getPath() + "/{id}")
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    protected void testUpdate_whileApiIsDisabled_405() throws JsonProcessingException {
        BaseEntity p = factory().insertEntity();
        ObjectMapper om = new ObjectMapper();
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", p.id)
                .body(om.writeValueAsString(p))
                .when()
                .put(factory().getPath() + "/{id}")
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.METHOD_NOT_ALLOWED);
    }

}