package com.ustadmobile.lib.contentscrapers.ck12.practice;

import com.google.gson.annotations.SerializedName;
import com.ustadmobile.lib.contentscrapers.ck12.practice.AnswerResponse;


import java.util.List;

public class QuestionResponse {

    public Response response;

    public class Response {

        @SerializedName("_id")
        public String id;

        @SerializedName("goal")
        public int goal;

        @SerializedName("nextPracticeUrl")
        public String nextPracticeUrl;

        @SerializedName("nextPracticeName")
        public String nextPracticeName;

        @SerializedName("practiceName")
        public String practiceName;

        @SerializedName("evalData")
        public String data;

        @SerializedName("questionID")
        public String questionID;

        @SerializedName("questionTypeName")
        public String questionType;

        public Question stem;

        boolean multiAnswers;

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

        public List<String> hints;

        public AnswerResponse answer;

    }

}
