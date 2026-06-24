package org.nmdp.miring;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * Enable CORS on POST, OPTIONS etc.
 * See SO link for more details:
 * https://stackoverflow.com/questions/28065963/how-to-handle-cors-using-jax-rs-with-jersey
 *
 * Note: This is a public validation API so we allow all origins,
 * but we do NOT set Allow-Credentials with wildcard origin (browser security violation).
 */
public class CORSFilter implements ContainerResponseFilter {
    @Override
    public ContainerResponse filter(ContainerRequest request,
                                    ContainerResponse response) {

        response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHttpHeaders().add("Access-Control-Allow-Headers",
                "origin, content-type, accept");
        response.getHttpHeaders().add("Access-Control-Allow-Methods",
                "POST, OPTIONS, HEAD");
        response.getHttpHeaders().add("Access-Control-Max-Age", "3600");

        return response;
    }
}
