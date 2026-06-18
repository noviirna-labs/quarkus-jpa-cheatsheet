package io.github.noviirna.resource;

import io.github.noviirna.entity.Course;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import org.eclipse.microprofile.openapi.annotations.Operation;


@ResourceProperties
public interface CourseResource extends PanacheEntityResource<Course, Long> {

    @Override
    @MethodProperties(exposed = false)
    @Operation(hidden = true)
    long count();
}
