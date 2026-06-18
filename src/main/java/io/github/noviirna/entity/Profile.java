package io.github.noviirna.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;

import java.io.Serial;


@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    /**
     * id will be returned as payload json in endpoint where it return Profile class,
     * but when received from an endpoint payload request as json, it will never be received to be transformed as java class
     */
    @Id
    @Column(name = "student_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long id;

    public String academicLevel;

    /**
     * Prevent lazy loading of student get invoked,
     * when serializing profile by set json access to write only
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @MapsId
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "student_id")
    @JsonUnwrapped
    public Student student;

    public Long getId() {
        return null == this.id ?
                null == this.student ? null : this.student.id
                : this.id;
    }

    public static Profile findByIdWithStudent(Long id) {
        return find("FROM Profile p LEFT JOIN FETCH p.student WHERE p.id = ?1", id)
                .firstResult();
    }
}
