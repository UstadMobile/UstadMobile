package com.ustadmobile.lib.contentscrapers.CK12;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnswerResponse {

    @SerializedName("instance")
    public Instance instance;

    public class Instance{

        String solution;

        boolean multiAnswers;

        String questionTypeName;

        List<AnswerObjects> responseObjects;

        List<Object> answer;

        public class AnswerObjects {

            String isCorrect;

            public int displayOrder;

            public String displayText;

            public String orderText;

            public String optionKey;

            public int ansSeq;

        }

    }

}
