package com.ustadmobile.lib.contentscrapers.ck12.practice;

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

            @SerializedName("questionsCount")
            public int questionsCount;

            public String title;

            public NextPractice nextPractice;

            public String updated;

            public class NextPractice {

                @SerializedName("_id")
                public String id;

                @SerializedName("handle")
                public String nameOfNextPractice;

            }
        }

    }

}
