package io.github.noviirna.resource;

import io.github.noviirna.entity.Enrollment;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import org.eclipse.microprofile.openapi.annotations.Operation;


@ResourceProperties
public interface EnrollmentResource extends PanacheEntityResource<Enrollment, Long> {

    @Override
    @MethodProperties(exposed = false)
    @Operation(hidden = true)
    long count();

    @Override
    @MethodProperties(exposed = false)
    @Operation(hidden = true)
    Enrollment add(Enrollment entity);


//    !TODO! CUSTOM INSERT
}
