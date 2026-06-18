package io.github.noviirna.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serial;


@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Prevent lazy loading of course when serializing enrollment
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    public Student student;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Prevent lazy loading of course when serializing enrollment
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    public Course course;


    /**
     * This method is used to get the student id of the profile.
     * The goal of this is to be used when returning response from Resource classes.
     * So the field will be included in the response, but it will not cause lazy loading of the student entity when serializing the profile.
     *
     * @return the student id of the profile
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Prevent lazy loading of course when serializing enrollment

    @Schema(examples = "123", description = "id of student")
    public Long getStudentId() {
        return this.student.id;
    }

    /**
     * This method is used to get the course id of the profile.
     * The goal of this is to be used when returning response from Resource classes.
     * So the field will be included in the response, but it will not cause lazy loading of the student entity when serializing the profile.
     *
     * @return the course id of the profile
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Prevent lazy loading of course when serializing enrollment
    @Schema(examples = "123", description = "id of course")
    public Long getCourseId() {
        return this.course.id;
    }
}
