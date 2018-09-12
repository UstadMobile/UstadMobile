package com.ustadmobile.lib.contentscrapers.ck12.plix;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlixQuestions {

    @Expose
    @SerializedName("response")
    private QuestionResponse response;


    public class QuestionResponse {

        public PlixTest test;

        public TestScore testScore;

        public class TestScore {

            List<Questions> submissions;

            public class Questions {

                public Response object;

                public class Response{

                    String evalData;

                    public List<String> hints;

                    @SerializedName("questionTypeName")
                    public String questionType;

                    @SerializedName("questionID")
                    public String questionID;

                    boolean multiAnswers;

                    public Question stem;

                    public class Question {

                        public String displayText;

                    }
                    public List<QuestionObjects> responseObjects;

                    public class QuestionObjects {

                        public int displayOrder;

                        public String displayText;

                        public String orderText;

                        public String optionKey;

                    }



                }


            }
        }

        public class PlixTest {
            @Expose
            @SerializedName("updated")
            private String updated;
            @Expose
            @SerializedName("title")
            private String title;
            @Expose
            @SerializedName("testTypeID")
            private String testTypeID;

            @Expose
            @SerializedName("questionsCount")
            private int questionsCount;

            @Expose
            @SerializedName("plixID")
            private String plixID;
            @Expose
            @SerializedName("ownerID")
            private String ownerID;

            @Expose
            @SerializedName("isPublic")
            private boolean isPublic;
            @Expose
            @SerializedName("handlelc")
            private String handlelc;
            @Expose
            @SerializedName("handle")
            private String handle;
            @Expose
            @SerializedName("encodedIDs")
            private List<String> encodedIDs;
            @Expose
            @SerializedName("description")
            private String description;
            @Expose
            @SerializedName("created")
            private String created;
            @Expose
            @SerializedName("_id")
            private String _id;
        }

    }


}
