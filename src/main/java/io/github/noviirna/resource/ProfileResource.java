package io.github.noviirna.resource;

import io.github.noviirna.dto.ProfileDto;
import io.github.noviirna.model.Profile;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;


@ResourceProperties
public interface ProfileResource extends PanacheEntityResource<Profile, Long> {

    /**
     * This method demonstrate how lazy load works and how it can lead to n+1 query problem.<br><br>
     * When system try to access the associated student of the profile during serialization to be a JSON (where all of the getter is called),
     * the lazy load will be started because system try to access the associated student's field besides student.id.<br><br>
     * During lazy load, hibernate will execute a separate query to fetch the student data for each profile to complete the data,
     * which can lead to n+1 query problem.<br>
     * <p>
     * That is why it is recommended to:<br>
     * <ul>
     *     <li>return Dto instead of entity directly, so we can control what data is returned and avoid lazy loading issues.</li>
     *     <li>use join fetch to retrieve the profile and its associated student in a single query, avoiding the n+1 query problem.</li>
     * </ul>
     * <p>
     * This endpoint is not recommended to be used in production, it is only for demonstration purpose.
     *
     * @param id
     * @return
     */
    @GET
    @Path("/profile/{id}/complete")
    @Produces("application/json")
    default ProfileDto getCompleteProfile(@PathParam("id") Long id) {
        Profile dao = Profile.findById(id);

        return new ProfileDto(dao.id,
                dao.academicLevel,
                dao.student,
                dao.student.id);
    }


    /**
     * This method demonstrates how to use a join fetch
     * to retrieve the profile and its associated student in a single query,
     * avoiding the n+1 query problem.
     *
     * @param id
     * @return
     */
    @GET
    @Path("/profile/{id}/complete-joinfetch")
    @Produces("application/json")
    default ProfileDto getCompleteProfileJoinFetch(@PathParam("id") Long id) {
        Profile dao = Profile.findByIdWithStudent(id);

        return new ProfileDto(dao.id,
                dao.academicLevel,
                dao.student,
                dao.student.id);
    }
}
