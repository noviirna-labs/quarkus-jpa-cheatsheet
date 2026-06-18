package io.github.noviirna.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serial;


@Entity
@Table(name = "students")
public class Student extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    @Schema(examples = "john doe")
    public String name;

}
