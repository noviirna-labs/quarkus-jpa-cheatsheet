package io.github.noviirna.resource.panacheentity.integration;

import io.github.noviirna.entity.Profile;
import io.github.noviirna.resource.panacheentity.RestPanacheTest;
import io.github.noviirna.resource.panacheentity.factory.ProfileFactory;
import io.github.noviirna.resource.panacheentity.RestPanacheTestFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

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

}