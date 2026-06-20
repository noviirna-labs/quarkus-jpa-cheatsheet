package io.github.noviirna.resource.it;

import io.github.noviirna.entity.Enrollment;
import io.github.noviirna.resource.base.RestPanacheTest;
import io.github.noviirna.resource.factory.EnrollmentFactory;
import io.github.noviirna.resource.factory.RestPanacheTestFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnrollmentIT extends RestPanacheTest<Enrollment> {

    @Inject
    EnrollmentFactory enrollmentFactory;

    @Override
    protected RestPanacheTestFactory<Enrollment> factory() {
        return enrollmentFactory;
    }


    @Override
    @Test
    protected void testPost_whileApiIsDisabled_405() {
        Assumptions.assumeTrue(
                false,
                "Enroll support Post, skip this test");
    }

    @Override
    @Test
    protected void testUpdate_success_204() {
        Assumptions.assumeTrue(
                false,
                "Enroll doesn't support update, skip this test");
    }

    @Override
    @Test
    protected void testUpdate_whileNoRecords_500() {
        Assumptions.assumeTrue(
                false,
                "Enroll doesn't support update, skip this test");
    }
}