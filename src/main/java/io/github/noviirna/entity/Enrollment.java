package io.github.noviirna.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serial;


/**
 * Junction entity deconstructing the {@code Student}–{@code Course} many-to-many
 * relationship into two one-to-many relationships (see {@code docs/usecase.md}
 * for the full before/after normalization writeup).
 * <p>
 * At the database level, {@code enrollments} is a pure aggregate table, just an
 * id plus foreign keys, no data of its own. The API shape follows that: the
 * {@code student} and {@code course} relations are hidden from JSON entirely
 * (see {@link #student} and {@link #course}) and replaced with flat {@code Long}
 * id fields ({@link #getStudentId()}, {@link #getCourseId()}). This matches what
 * a database wrapper service should expose, callers are linking records by id,
 * not exchanging full {@code Student}/{@code Course} objects through this table.
 */
@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 5716107637584368332L;

    //region custom getter setter

    /**
     * Reads the linked student's id directly off the (otherwise hidden)
     * {@link #student} relation, so it can be exposed in the response body
     * as a plain {@code Long} instead of a nested {@code Student} object.
     */
    @Schema(examples = "123", description = "id of student")
    public Long getStudentId() {
        return this.student.id;
    }

    /**
     * Accepts a plain student id from the request body and wraps it into a
     * lazy-reference {@code Student} stub (only {@code id} is set) so JPA can
     * resolve the {@code @ManyToOne} relation without needing the full
     * {@code Student} object in the payload.
     */
    public void setStudentId(Long id) {
        if (this.student == null) this.student = new Student();
        this.student.id = id;
    }

    /**
     * Reads the linked course's id directly off the (otherwise hidden)
     * {@link #course} relation, so it can be exposed in the response body
     * as a plain {@code Long} instead of a nested {@code Course} object.
     */
    @Schema(examples = "123", description = "id of course")
    public Long getCourseId() {
        return this.course.id;
    }

    /**
     * Accepts a plain course id from the request body and wraps it into a
     * lazy-reference {@code Course} stub (only {@code id} is set) so JPA can
     * resolve the {@code @ManyToOne} relation without needing the full
     * {@code Course} object in the payload.
     */
    public void setCourseId(Long id) {
        if (this.course == null) this.course = new Course();
        this.course.id = id;
    }
    //endregion custom getter setter

    //region foreign keys
    /**
     * The enrolled student. Hidden from JSON entirely ({@code @JsonIgnore}) and
     * never appears in request or response bodies. Exposed externally only as
     * a flat id via {@link #getStudentId()} / {@link #setStudentId(Long)}.
     */
    @JsonIgnore
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    public Student student;


    /**
     * The enrolled course. Hidden from JSON entirely ({@code @JsonIgnore}) and
     * never appears in request or response bodies. Exposed externally only as
     * a flat id via {@link #getCourseId()} / {@link #setCourseId(Long)}.
     */
    @JsonIgnore
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    public Course course;
    //endregion

}