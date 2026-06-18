package io.github.noviirna.resource.it;

import io.github.noviirna.entity.Student;
import io.github.noviirna.resource.base.RestPanacheTest;
import io.github.noviirna.resource.factory.RestPanacheTestFactory;
import io.github.noviirna.resource.factory.StudentFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class StudentIT extends RestPanacheTest<Student> {

    @Inject
    StudentFactory studentFactory;

    @Override
    protected RestPanacheTestFactory<Student> factory() {
        return studentFactory;
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

    @Test
    protected void testHardDelete_whileApiIsDisabled_405() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 1l)
                .when()
                .delete(studentFactory.getPath() + "/{id}")
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.METHOD_NOT_ALLOWED);
    }

    // !TODO! NEXT: it post complete student data
    // !TODO! NEXT: it delete complete student data

}