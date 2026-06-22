package io.github.noviirna.resource.panacheentity.factory;

import io.github.noviirna.entity.Profile;
import io.github.noviirna.entity.Student;
import io.github.noviirna.resource.panacheentity.RestPanacheTestFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProfileFactory implements RestPanacheTestFactory<Profile> {

    @Override
    public String getPath() {
        return "/profile";
    }

    @Transactional
    @Override
    public void purgeEntity() {
        Profile.deleteAll();
    }

    @Transactional
    @Override
    public Profile insertEntity() {
        Student s = new Student();
        s.name = "john doe";
        s.persist();

        Profile p = new Profile();
        p.student = s;
        p.academicLevel = "junior";
        p.id = s.id;
        p.persist();
        return p;
    }

    @Transactional
    @Override
    public void delete(Profile p) {
        p.delete();
    }

}