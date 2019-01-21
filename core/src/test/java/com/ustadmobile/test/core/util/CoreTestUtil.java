package com.ustadmobile.test.core.util;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class CoreTestUtil {

    public static final String TEST_URI = "http://localhost:8089/api/";

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.core.db.dao")
                .register(MultiPartFeature.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

}
