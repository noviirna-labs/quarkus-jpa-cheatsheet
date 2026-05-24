package io.github.noviirna.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;


@Entity
@Table(name = "students")
public class Student extends PanacheEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    public String name;

}
