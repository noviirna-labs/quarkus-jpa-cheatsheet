package io.github.noviirna.resource.factory;

import io.github.noviirna.entity.Course;
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

}