package com.ustadmobile.lib.contentscrapers.CK12;

import com.google.gson.annotations.SerializedName;


import java.util.List;

public class QuestionResponse {

    public Response response;

    public class Response {

        @SerializedName("_id")
        public String id;

        @SerializedName("evalData")
        public String data;

        @SerializedName("questionID")
        public String questionID;

        @SerializedName("questionTypeName")
        public String questionType;

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

        public List<String> hints;

        public AnswerResponse answer;

    }

}
