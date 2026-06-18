package io.github.noviirna.resource.factory;

import io.github.noviirna.entity.Profile;
import io.github.noviirna.entity.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StudentFactory implements RestPanacheTestFactory<Student> {

    @Override
    public String getPath() {
        return "/student";
    }

    @Transactional
    @Override
    public void purgeEntity() {
        Profile.deleteAll();
        Student.deleteAll();
    }

    @Transactional
    @Override
    public Student insertEntity() {
        Student s = new Student();
        s.name = "john doe";
        s.persist();

        Profile p = new Profile();
        p.student = s;
        p.academicLevel = "junior";
        p.id = s.id;
        p.persist();
        return s;
    }

}