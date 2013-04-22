package org.pinion.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class ExampleResource {
    public ExampleResource() {

    }

    @GET
    public Response get() {
        return Response.ok().build();
    }

}
