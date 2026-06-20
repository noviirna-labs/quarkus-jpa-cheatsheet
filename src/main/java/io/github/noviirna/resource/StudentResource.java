package io.github.noviirna.resource;

import io.github.noviirna.entity.Student;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;


@ResourceProperties
public interface StudentResource extends PanacheEntityResource<Student, Long> {

    // region hidden endpoints

    /**
     * <b>Disabled</b><br>
     * See {@linkplain  CourseResource#count()} for rationale.<p>
     * Applied consistently across all entities in this template.
     */
    @Override
    @MethodProperties(exposed = false)
    long count();

    /**
     * <b>Disabled</b><br>
     * A {@code Student} has two dependents that block a plain delete:
     * <ul>
     *     <li>{@code Profile} shares its primary key with {@code Student} via
     *     {@code @MapsId}. Deleting normally requires nulling the child's FK first,
     *     but here the <u>FK is the PK</u>, so nulling it would violate the primary
     *     key constraint itself. This always blocks deletion if a {@code Profile}
     *     exists, there's no conditional case.</li>
     *     <li>{@code Enrollment} a regular foreign key, not a shared primary key.
     *     Unlike {@code Profile}, this only blocks deletion if the {@code Student}
     *     currently has any enrollments; an unenrolled {@code Student} without a
     *     {@code Profile} could otherwise be deleted normally.</li>
     * </ul>
     * <p>
     * Recommended order to fully remove a {@code Student} and its dependents:
     * <ol>
     *     <li>{@link EnrollmentResource#bulkDeleteByStudentId}: clears any
     *     {@code Enrollment} rows referencing this student.</li>
     *     <li>{@link ProfileResource#deleteAggregate(long)}: deletes
     *     {@code Profile} and {@code Student} together atomically.</li>
     * </ol>
     */
    @Override
    @MethodProperties(exposed = false)
    boolean delete(Long id);
    // endregion hidden endpoints


}
