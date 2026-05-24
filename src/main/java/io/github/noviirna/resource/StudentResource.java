package io.github.noviirna.resource;

import io.github.noviirna.model.Student;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;


@ResourceProperties
public interface StudentResource extends PanacheEntityResource<Student, Long> {

}
