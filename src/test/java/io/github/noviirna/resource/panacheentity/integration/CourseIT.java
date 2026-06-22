package io.github.noviirna.resource.panacheentity.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.noviirna.entity.Course;
import io.github.noviirna.resource.panacheentity.RestPanacheTest;
import io.github.noviirna.resource.panacheentity.factory.CourseFactory;
import io.github.noviirna.resource.panacheentity.RestPanacheTestFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class CourseIT extends RestPanacheTest<Course> {

    @Inject
    CourseFactory courseFactory;

    @Override
    protected RestPanacheTestFactory<Course> factory() {
        return courseFactory;
    }


    @Override
    @Test
    protected void testPost_whileApiIsDisabled_405() {
        Assumptions.assumeTrue(
                false,
                "Course support Post, skip this test");
    }


    @Test
    protected void testPost_isSuccess_201() throws JsonProcessingException {
        Course p = factory().insertEntity();
        p.id = null;
        ObjectMapper om = new ObjectMapper();
        given()
                .contentType(ContentType.JSON)
                .body(om.writeValueAsString(p))
                .when()
                .post(factory().getPath())
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.CREATED);
    }


    @Test
    protected void testPost_invalidParameter_400() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        given()
                .contentType(ContentType.JSON)
                .body("[{]")
                .when()
                .post(factory().getPath())
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.BAD_REQUEST);
    }
}