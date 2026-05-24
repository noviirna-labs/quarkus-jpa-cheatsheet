package io.github.noviirna.resource;

import io.github.noviirna.model.Course;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;


@ResourceProperties
public interface CourseResource extends PanacheEntityResource<Course, Long> {

}
