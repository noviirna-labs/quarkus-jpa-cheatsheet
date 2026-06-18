package io.github.noviirna.config.oas;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;

import static io.github.noviirna.config.oas.OASFilter.REST_PANACHE_ENDPOINTS;

public class OASFilterHelperPanache {

    boolean supports(String path) {
        return REST_PANACHE_ENDPOINTS.stream().anyMatch(e -> e.equals(path));
    }

    Paths applyCustomization(Paths paths, String path) {
        if (!supports(path)) return paths;

        boolean hasPathParam = path.contains("{") && path.contains("}");

        Paths newPaths = null;
        if (hasPathParam)
            newPaths = processPathWithParam(paths, path);
        else
            newPaths = processPaths(paths, path);

        return newPaths;
    }


    private Paths processPaths(Paths originPaths, String endpoint) {
        PathItem item = originPaths.getPathItem(endpoint);
        if (item == null) return originPaths;

        item.setGET(adjustGetApiResponseMultiple(item));
        originPaths.removePathItem(endpoint);
        originPaths.addPathItem(endpoint, item);

        return originPaths;
    }

    private Paths processPathWithParam(Paths originPaths, String endpoint) {
        PathItem item = originPaths.getPathItem(endpoint);
        if (item == null) return originPaths;

        item.setPUT(adjustPutApiResponse(item));
        item.setDELETE(adjustDeleteApiResponse(item));
        item.setGET(adjustGetApiResponseSingle(item));

        originPaths.removePathItem(endpoint);
        originPaths.addPathItem(endpoint, item);

        return originPaths;
    }

    private Operation adjustPutApiResponse(PathItem pi) {
        Operation origin = pi.getPUT();
        if (origin == null) return origin;

        origin.getResponses().removeAPIResponse("201");
        origin.getResponses().removeAPIResponse("400");

        origin.getResponses().addAPIResponse("204", getAPIResponseUpdateSuccess());
        origin.getResponses().addAPIResponse("404", getAPIResponseNotFound());

        return origin;
    }

    private Operation adjustGetApiResponseSingle(PathItem pi) {
        Operation op = pi.getGET();

        if (op == null) return op;

        op.getResponses().addAPIResponse("404", getAPIResponseNotFound());

        return op;
    }

    private Operation adjustGetApiResponseMultiple(PathItem pi) {
        Operation op = pi.getGET();

        if (op == null) return op;

        op.getResponses().addAPIResponse("404", getAPIResponseNotFound());

        return op;
    }

    private Operation adjustDeleteApiResponse(PathItem pi) {
        Operation op = pi.getDELETE();

        if (op == null) return op;

        op.getResponses().removeAPIResponse("201");

        op.getResponses().addAPIResponse("204", getAPIResponseUpdateSuccess());
        op.getResponses().addAPIResponse("404", getAPIResponseNotFound());

        op.getResponses().setDefaultValue(getAPIResponseUpdateSuccess());

        return op;
    }

    private APIResponse getAPIResponseUpdateSuccess() {
        APIResponse updated = OASFactory.createAPIResponse()
                .description("Successfully update data");
        return updated;
    }

    private APIResponse getAPIResponseNotFound() {
        APIResponse notfound = OASFactory.createAPIResponse()
                .description("Not Found");
        return notfound;
    }

}
