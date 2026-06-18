package io.github.noviirna.config.oas;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;

import java.util.List;


@OpenApiFilter(stages =
        {
                OpenApiFilter.RunStage.RUNTIME_STARTUP})
public class OASFilter implements org.eclipse.microprofile.openapi.OASFilter {

    public static final List<String> REST_PANACHE_ENDPOINTS =
            List.of("/course",
                    "/course/{id}",
                    "/enrollment",
                    "/enrollment/{id}",
                    "/profile",
                    "/profile/{id}",
                    "/student",
                    "/student/{id}");

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        if (openAPI.getPaths() != null) {
            for (String panacheEndpoint : REST_PANACHE_ENDPOINTS) {
                Paths p = openAPI.getPaths();
                Paths newP = new OASFilterHelperPanache().applyCustomization(p, panacheEndpoint);
                p = newP;
                openAPI.setPaths(p);
            }
        }
    }


}