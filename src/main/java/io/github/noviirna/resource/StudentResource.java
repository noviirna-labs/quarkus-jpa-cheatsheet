package io.github.noviirna.resource;

import io.github.noviirna.dto.StudentProfileDto;
import io.github.noviirna.entity.Profile;
import io.github.noviirna.entity.Student;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;


@ResourceProperties
public interface StudentResource extends PanacheEntityResource<Student, Long> {


    @Override
    @MethodProperties(exposed = false)
    Student add(Student request);

    @Override
    @MethodProperties(exposed = false)
    @Operation(hidden = true)
    boolean delete(Long id);

    @Override
    @MethodProperties(exposed = false)
    @Operation(hidden = true)
    long count();

    /**
     * tidak bisa override pakai path yg sama walau udah di inaktifin dan udh ganti path si default method dari interface nya
     *
     * @param request
     * @return
     */
    @Transactional
    @POST
    @Path("/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    default StudentProfileDto add2(@RequestBody StudentProfileDto request) {
        Student s = new Student();
        s.name = request.student().name;
        s.persist();

        Profile p = new Profile();
        p.student = s;
        p.academicLevel = request.academicLevel();
        p.id = s.id;
        p.persist();

        return new StudentProfileDto(p.academicLevel, s);
    }

    /**
     *
     * @param id
     * @return
     */
    @Transactional
    @DELETE
    @Path("/{id}/complete")
    @Produces(MediaType.APPLICATION_JSON)
    default Response delete2(@PathParam("id") Long id) {
        if (!Profile.deleteById(id))
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        else
            return Response.status(Response.Status.NO_CONTENT)
                    .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    default Response update2(@PathParam("id") Long id, StudentProfileDto request) {
        // Cek parent ada atau tidak
        Student parent = Student.findById(request.student().id);
        if (parent == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Cek child sendiri ada atau tidak
        Profile existing = Profile.findById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        // Lakukan update
        existing.student = request.student();
        existing.academicLevel = null != request.academicLevel()
                ? request.academicLevel()
                : existing.academicLevel;
        existing.persist();

        return Response.ok(existing).build();
    }
}
