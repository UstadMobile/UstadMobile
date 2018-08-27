package com.ustadmobile.lib.contentscrapers.CK12;

import com.google.gson.annotations.SerializedName;

public class TestResponse {

    public Response response;

    public class Response{

        public Test test;
        public TestScore testScore;

        public class Test {

            @SerializedName("_id")
            public String id;

        }

        public class TestScore{

            @SerializedName("_id")
            public String id;

        }

    }

}
