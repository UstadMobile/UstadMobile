package com.ustadmobile.lib.contentscrapers.ck12.practice;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnswerResponse {

    @SerializedName("instance")
    public Instance instance;

    public class Instance{

        public String solution;

        boolean multiAnswers;

        String questionTypeName;

        public List<AnswerObjects> responseObjects;

        public List<Object> answer;

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
