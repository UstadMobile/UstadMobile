package com.ustadmobile.lib.contentscrapers.voa;

import java.util.List;

public class VoaQuiz {

    public String quizId;

    public List<Questions> questions;

    public static class Questions {

        public String questionText;

        public String videoHref;

        public String answerId;

        public String answer;

        public List<Choices> choices;

        public static class Choices {

            public String id;

            public String answerText;

        }
    }
}
