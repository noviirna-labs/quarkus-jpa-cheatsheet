package io.github.noviirna.resource.panacheentity.integration;

import io.github.noviirna.entity.Enrollment;
import io.github.noviirna.resource.panacheentity.RestPanacheTest;
import io.github.noviirna.resource.panacheentity.RestPanacheTestFactory;
import io.github.noviirna.resource.panacheentity.factory.EnrollmentFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class EnrollmentIT extends RestPanacheTest<Enrollment> {

    @Inject
    EnrollmentFactory enrollmentFactory;

    private static final String PATH_STUDENT = "/student";
    private static final String PATH_COURSE = "/course";

    private static final String PATHPARAM_STUDENTID = "/{student_id}";
    private static final String PATHPARAM_COURSEID = "/{course_id}";

    private static final String FIELDNAME_STUDENTID = "student_id";
    private static final String FIELDNAME_COURSEID = "course_id";


    @Override
    protected RestPanacheTestFactory<Enrollment> factory() {
        return enrollmentFactory;
    }


    @Test
    protected void testHardDeleteByStudentId_success_204() {
        Enrollment p = factory().insertEntity();
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_STUDENTID, p.student.id)
                .when()
                .delete(factory().getPath() + PATH_STUDENT + PATHPARAM_STUDENTID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.OK)
                .body(Matchers.equalTo("1"));
    }

    @Test
    protected void testHardDeleteByStudentId_isNotFound_404() {
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_STUDENTID, VALUE_ID)
                .when()
                .delete(factory().getPath() + PATH_STUDENT + PATHPARAM_STUDENTID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.OK)
                .body(Matchers.equalTo("0"));
    }

    @Test
    protected void testHardDeleteByCourseId_success_204() {
        Enrollment p = factory().insertEntity();
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_COURSEID, p.course.id)
                .when()
                .delete(factory().getPath() + PATH_COURSE + PATHPARAM_COURSEID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.OK)
                .body(Matchers.equalTo("1"));
    }

    @Test
    protected void testHardDeleteByCourseId_isNotFound_404() {
        given()
                .contentType(ContentType.JSON)
                .pathParam(FIELDNAME_COURSEID, VALUE_ID)
                .when()
                .delete(factory().getPath() + PATH_COURSE + PATHPARAM_COURSEID)
                .then()
                .assertThat()
                .statusCode(RestResponse.StatusCode.OK)
                .body(Matchers.equalTo("0"));

    }

}