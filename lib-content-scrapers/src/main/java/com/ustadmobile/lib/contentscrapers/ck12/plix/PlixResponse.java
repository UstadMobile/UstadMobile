package com.ustadmobile.lib.contentscrapers.ck12.plix;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlixResponse {

    @Expose
    @SerializedName("response")
    public Response response;

    public class Response {
        @Expose
        @SerializedName("test")
        public Test test;

        public class Test {
            
            @Expose
            @SerializedName("_id")
            public String _id;
            
            @Expose
            @SerializedName("updated")
            public String updated;
            @Expose
            @SerializedName("title")
            public String title;
            @Expose
            @SerializedName("testTypeID")
            public String testTypeID;
            
            @Expose
            @SerializedName("questionsCount")
            public int questionsCount;
            @Expose
            @SerializedName("plixID")
            public String plixID;
            @Expose
            @SerializedName("ownerID")
            public String ownerID;

            @Expose
            @SerializedName("isPublic")
            public boolean isPublic;
            @Expose
            @SerializedName("handlelc")
            public String handlelc;
            @Expose
            @SerializedName("handle")
            public String handle;
            @Expose
            @SerializedName("encodedIDs")
            public List<String> encodedIDs;
            @Expose
            @SerializedName("description")
            public String description;
            @Expose
            @SerializedName("created")
            public String created;
            @Expose
            @SerializedName("attemptsCount")
            public int attemptsCount;
            
        }

    }


}
