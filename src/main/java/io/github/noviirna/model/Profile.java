package io.github.noviirna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;


@Entity
@Table(name = "profiles")
public class Profile extends PanacheEntityBase implements Serializable {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    @Id
    @Column(name = "student_id")
    public Long id;

    public String academicLevel;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Prevent lazy loading of student when serializing profile
    @MapsId
    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    public Student student;

    /**
     * This method is used to get the student id of the profile.
     * The goal of this is to be used when returning response from Resource classes.
     * So the field will be included in the response, but it will not cause lazy loading of the student entity when serializing the profile.
     * @return the student id of the profile
     */
    public Long getStudentId() {
        return this.student.id;
    }

    public static Profile findByIdWithStudent(Long id) {
        return find("FROM Profile p LEFT JOIN FETCH p.student WHERE p.id = ?1", id)
                .firstResult();
    }
}
