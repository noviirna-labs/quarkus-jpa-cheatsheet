package io.github.noviirna.resource.it;

import io.github.noviirna.entity.Enrollment;
import io.github.noviirna.resource.base.RestPanacheTest;
import io.github.noviirna.resource.factory.EnrollmentFactory;
import io.github.noviirna.resource.factory.RestPanacheTestFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class EnrollmentIT extends RestPanacheTest<Enrollment> {

    @Inject
    EnrollmentFactory enrollmentFactory;

    @Override
    protected RestPanacheTestFactory<Enrollment> factory() {
        return enrollmentFactory;
    }

//    !TODO! CUSTOM POST IT
}