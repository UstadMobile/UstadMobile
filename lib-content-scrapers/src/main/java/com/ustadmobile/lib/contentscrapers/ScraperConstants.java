package com.ustadmobile.lib.contentscrapers;

import java.util.Arrays;
import java.util.List;

public class ScraperConstants {

    public static final String CONTENT_JSON = "content.json";
    public static final String QUESTIONS_JSON = "questions.json";
    public static final String VIDEO_MP4 = "video.mp4";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String IMG_EXT = "eximage.png";
    public static final String IMG_TAG = "img";
    public static final String JS_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/index.html";
    public static final String JS_TAG = "/com/ustadmobile/lib/contentscrapers/jquery-3.3.1.min.js";

    public static final List<String> QUESTION_SET_HOLDER_TYPES = Arrays.asList(
           ComponentType.EXCERCISE.getType(), ComponentType.ONLINE.getType(),
           ComponentType.TEST.getType());


    public static final String PNG_EXT = ".png";

    public enum ComponentType{
        MAIN("MainContentTrack"),
        SECTION("Section"),
        SUBSECTION("SubSection"),
        IMPORTED("ImportedComponent"),
        MULTICHOICE("MultipleChoiceQuestion"),
        QUESTIONSET("QuestionSet"),
        TEST("Test"),
        VIDEO("Video"),
        EXCERCISE("Exercise"),
        NUMERIC_QUESTION("NumericResponseQuestion"),
        ONLINE("OnlineLesson");

        private String type;

        ComponentType(String compType) {
            this.type = compType;
        }

        public String getType() {
            return type;
        }
    }

    public enum HtmlName{
        DESC("description"),
        FULL_DESC("full_description"),
        EXPLAIN("explaination"),
        CHOICE("choice"),
        HINT("hint");

        private String name;

        HtmlName(String compType) {
            this.name = compType;
        }

        public String getName() {
            return name;
        }

    }

}
