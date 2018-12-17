package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao_Retrofit;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.WebTarget;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class TestRetrofitInterface {

    private HttpServer server;

    private ExampleSyncableDao_Retrofit exampleSyncableDaoRetrofit;

    public static final String TEST_URI = "http://localhost:8089/api/";

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.lib.annotationprocessor.core.db");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    @Before
    public void setup() throws IOException {
        server =startServer();
        server.start();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(TEST_URI)
                .build();
        exampleSyncableDaoRetrofit = retrofit.create(ExampleSyncableDao_Retrofit.class);
    }

    @After
    public void stop() {
        server.stop();
    }


    @Test
    public void testGetString() throws IOException {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();

        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setTitle("Get Retrofit");
        long uid = db.getExampleSyncableDao().insert(e1);

        String rTitle = exampleSyncableDaoRetrofit.getTitleByUid(uid).execute().body();
        Assert.assertEquals("Received expected title", "Get Retrofit", rTitle);
    }

    @Test
    public void testGetObject() throws IOException {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setTitle("Get Retrofit");
        long uid = db.getExampleSyncableDao().insert(e1);

        Call<List<ExampleSyncableEntity>> call = exampleSyncableDaoRetrofit.findAllLive();
        Response<List<ExampleSyncableEntity>> response = call.execute();
        List<ExampleSyncableEntity> list =  response.body();

        Assert.assertTrue("Got list", !list.isEmpty());
    }







}
