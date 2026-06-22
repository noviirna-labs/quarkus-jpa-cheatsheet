package io.github.noviirna.resource.panacheentity.factory;

import io.github.noviirna.entity.Course;
import io.github.noviirna.entity.Enrollment;
import io.github.noviirna.resource.panacheentity.RestPanacheTestFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalTime;

@ApplicationScoped
public class CourseFactory implements RestPanacheTestFactory<Course> {

    @Override
    public String getPath() {
        return "/course";
    }

    @Transactional
    @Override
    public void purgeEntity() {
        Course.deleteAll();
    }

    @Transactional
    @Override
    public Course insertEntity() {
        Course c = new Course();
        c.schedule = LocalTime.MAX;
        c.subject = "math";
        c.persist();
        return c;
    }

    @Transactional
    @Override
    public void delete(Course c) {
        Enrollment.delete("course.id", c.id);
        c.delete();
    }
}