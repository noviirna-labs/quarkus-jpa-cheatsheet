package io.github.noviirna.resource.panacheentity.factory;

import io.github.noviirna.entity.Course;
import io.github.noviirna.entity.Enrollment;
import io.github.noviirna.entity.Profile;
import io.github.noviirna.entity.Student;
import io.github.noviirna.resource.panacheentity.RestPanacheTestFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalTime;

@ApplicationScoped
public class EnrollmentFactory implements RestPanacheTestFactory<Enrollment> {

    @Override
    public String getPath() {
        return "/enrollment";
    }

    @Transactional
    @Override
    public void purgeEntity() {
        Enrollment.deleteAll();
        Profile.deleteAll();
        Student.deleteAll();
        Course.deleteAll();
    }

    @Transactional
    @Override
    public Enrollment insertEntity() {
        Student s = new Student();
        s.name = "john doe";
        s.persist();

        Profile p = new Profile();
        p.student = s;
        p.academicLevel = "junior";
        p.id = s.id;
        p.persist();

        Course c = new Course();
        c.schedule = LocalTime.MAX;
        c.subject = "math";
        c.persist();

        Enrollment e = new Enrollment();
        e.course = c;
        e.student = s;
        e.persist();
        return e;
    }

    @Transactional
    @Override
    public void delete(Enrollment e) {
        e.delete();
    }

}