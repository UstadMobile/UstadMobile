package com.ustadmobile.lib.contentscrapers;

import java.awt.image.DirectColorModel;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class ScraperConstants {

    public static final String CONTENT_JSON = "content.json";
    public static final String QUESTIONS_JSON = "questions.json";
    public static final String ETAG_TXT = "etag.txt";
    public static final String LAST_MODIFIED_TXT = "last-modified.txt";
    public static final String ABOUT_HTML = "about.txt";
    public static final String VIDEO_MP4 = "video.mp4";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String IMG_EXT = "eximage.png";
    public static final String IMG_TAG = "img";
    public static final String DIRECTORY = "/com/ustadmobile/lib/contentscrapers/";
    public static final String JS_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/index.html";
    public static final String JS_TAG = "/com/ustadmobile/lib/contentscrapers/jquery-3.3.1.min.js";
    public static final String MATERIAL_JS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.js";
    public static final String MATERIAL_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.css";
    public static final String REGULAR_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/DroidNaskh-Regular.woff2";
    public static final String BOLD_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/DroidNaskh-Bold.woff2";
    public static final String MATERIAL_JS = "materialize.min.js";
    public static final String MATERIAL_CSS = "materialize.min.css";

    public static final List<String> QUESTION_SET_HOLDER_TYPES = Arrays.asList(
           ComponentType.EXCERCISE.getType(), ComponentType.ONLINE.getType(),
           ComponentType.TEST.getType());


    public static final String PNG_EXT = ".png";
    public static final String ARABIC_FONT_REGULAR = "DroidNaskh-Regular.woff2";
    public static final String ARABIC_FONT_BOLD = "DroidNaskh-Bold.woiff2";
    public static final String INDEX_HTML = "index.html";
    public static final String JQUERY_JS = "jquery-3.3.1.min.js";

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(""
            + "[yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z']"
            + "[yyyy-MM-dd'T'HH:mm:ss.SSSSSS]"
            + "[yyyy-MM-dd'T'HH:mm:ss]"
    );


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
