package com.ustadmobile.lib.contentscrapers.ck12;

import com.google.gson.annotations.SerializedName;

public class PracticeResponse {

    public TestResponse response;

    public class TestResponse {

        public Test test;

        public class Test{

            @SerializedName("_id")
            public String id;

            @SerializedName("goal")
            public int goal;


        }

    }

}
