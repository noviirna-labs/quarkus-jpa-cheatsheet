package io.github.noviirna.resource;

import io.github.noviirna.entity.Course;
import io.github.noviirna.entity.Enrollment;
import io.github.noviirna.entity.Student;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;


@ResourceProperties
public interface EnrollmentResource extends PanacheEntityResource<Enrollment, Long> {

    // region hidden endpoints

    /**
     * <b>Disabled</b><br>
     * See {@linkplain CourseResource#count()} for rationale.<p>
     * Applied consistently across all entities in this template.
     */
    @Override
    @MethodProperties(exposed = false)
    long count();

    @Override
    @MethodProperties(exposed = false)
    Enrollment add(Enrollment entity);

    @Override
    @MethodProperties(exposed = false)
    Enrollment update(Long id, Enrollment entity);
    // endregion hidden endpoints

    //region custom endpoints
    @Transactional
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    default Response addFixed(@RequestBody @Valid Enrollment e) {
        e.student = Student.getEntityManager().getReference(Student.class, e.getStudentId());
        e.course = Course.getEntityManager().getReference(Course.class, e.getCourseId());

        e.persist();
        return Response.created(UriBuilder.fromMethod(EnrollmentResource.class, "addFixed").build())
                .entity(e).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    default Response updateFixed(@PathParam("id") Long id, @Valid Enrollment e) {
        e.id = id;
        e.student = Enrollment.getEntityManager().getReference(Student.class, e.student.id);
        e.course = Enrollment.getEntityManager().getReference(Course.class, e.course.id);
        Enrollment.getEntityManager().merge(e);
        return Response.noContent().build();
    }


    /**
     * Bulk-deletes all {@code Enrollment} rows linked to a given {@code Student},
     * in a single query rather than fetching and deleting rows one at a time.
     * <p>
     * Used as a prerequisite step before {@link ProfileResource#deleteAggregate(long)}
     * when the {@code Student} has existing enrollments, since {@code Student} can't
     * be deleted while {@code Enrollment} rows still reference it.
     *
     * @param studentId the {@code Student} whose enrollments should all be removed
     * @return the number of {@code Enrollment} rows deleted
     */
    @DELETE
    @Path("/student/{student_id : \\d+}")
    @Transactional
    default Response bulkDeleteByStudentId(@PathParam("student_id") Long studentId) {
        return Response.ok(Enrollment.delete("student.id", studentId)).build();

    }


    /**
     * Bulk-deletes all {@code Enrollment} rows linked to a given {@code Course},
     * in a single query rather than fetching and deleting rows one at a time.
     * <p>
     * Used as a prerequisite step before deleting a {@code Course} that still has
     * enrollments, see {@code CourseResource#delete}.
     *
     * @param courseId the {@code Course} whose enrollments should all be removed
     * @return the number of {@code Enrollment} rows deleted
     */
    @DELETE
    @Path("/course/{course_id : \\d+}")
    @Transactional
    default Response deleteByCourseId(@PathParam("course_id") Long courseId) {
        return Response.ok(Enrollment.delete("course.id", courseId)).build();
    }
    //endregion custom endpoints
}
