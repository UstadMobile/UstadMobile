package com.ustadmobile.lib.contentscrapers

import java.util.*

object ScraperConstants {

    val CONTENT_JSON = "content.json"
    val QUESTIONS_JSON = "questions.json"
    val ETAG_TXT = "etag.txt"
    val LAST_MODIFIED_TXT = "last-modified.txt"
    val ABOUT_HTML = "about.txt"

    val UTF_ENCODING = "UTF-8"
    val EDRAAK_INDEX_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/edraak/index.html"
    val CK12_INDEX_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/ck12/index.html"
    val JS_TAG = "/com/ustadmobile/lib/contentscrapers/jquery-3.3.1.min.js"
    val MATERIAL_JS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.js"
    val MATERIAL_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.css"
    val REGULAR_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/DroidNaskh-Regular.woff2"
    val BOLD_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/DroidNaskh-Bold.woff2"
    val CIRCULAR_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/css-circular-prog-bar.css"
    val LANGUAGE_LIST_LOCATION = "/com/ustadmobile/lib/contentscrapers/iso_639_3.json"
    val CIRCULAR_CSS_NAME = "css-circular-prog-bar.css"
    val TIMER_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/timer.svg"
    val TIMER_NAME = "timer.svg"
    val TROPHY_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/trophy.svg"
    val TROPHY_NAME = "trophy.svg"
    val CHECK_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/check.svg"
    val CHECK_NAME = "check.svg"
    val XML_NAMESPACE = "http://purl.org/dc/elements/1.1/"

    val KHAN_USERNAME = "samih@ustadmobile.com"
    val KHAN_PASS = "ustadscraper"
    val KHAN_LOGIN_LINK = "https://www.khanacademy.org/login"
    val ANDROID_USER_AGENT = "user-agent=Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36"

    val GRAPHIE = "+graphie"
    val KHAN_GRAPHIE_PREFIX = "https://cdn.kastatic.org/ka-perseus-graphie/"


    val EDRAAK_JS_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/edraak.min.js"
    val EDRAAK_JS_FILENAME = "edraak.min.js"

    val EDRAAK_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/edraak.min.css"
    val EDRAAK_CSS_FILENAME = "edraak.min.css"

    val VOA_JS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voa.min.js"
    val VOA_JS_FILE_NAME = "voa.min.js"

    val VOA_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voa.min.css"
    val VOA_CSS_FILE_NAME = "voa.min.css"

    val VOA_QUIZ_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voaquiz.min.css"
    val VOA_QUIZ_CSS_FILE_NAME = "voaquiz.min.css"

    val VOA_QUIZ_JS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voaquiz.min.js"
    val VOA_QUIZ_JS_FILE_NAME = "voaquiz.min.js"

    val CORRECT_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/exercise-correct.svg"
    val CORRECT_FILE = "exercise-correct.svg"

    val ATTEMPT_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/star-attempt.svg"
    val ATTEMPT_FILE = "star-attempt.svg"

    val COMPLETE_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/star-complete.svg"
    val COMPLETE_FILE = "star-complete.svg"

    val KHAN_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/khan/khanscraper.css"
    val KHAN_CSS_FILE = "khanscraper.css"

    val TRY_AGAIN_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/exercise-try-again.svg"
    val TRY_AGAIN_FILE = "exercise-try-again.svg"

    val QUIZ_HTML_LINK = "/com/ustadmobile/lib/contentscrapers/voa/quiz.html"
    val QUIZ_HTML_FILE = "quiz.html"

    val IFRAME_RESIZE_LINK = "/com/ustadmobile/lib/contentscrapers/voa/iframeResizer.min.js"
    val IFRAME_RESIZE_FILE = "iframeResizer.min.js"

    val IFRAME_RESIZE_WINDOW_LINK = "/com/ustadmobile/lib/contentscrapers/voa/iframeResizer.contentWindow.min.js"
    val IFRAME_RESIZE_WINDOW_FILE = "iframeResizer.contentWindow.min.js"

    // math jax links
    val MATH_JAX_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/MathJax.js"
    val MATH_JAX_FILE = "MathJax.js"

    val JAX_CONFIG_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/TeX-AMS-MML_HTMLorMML.js"
    val JAX_CONFIG_FILE = "/config/TeX-AMS-MML_HTMLorMML.js"

    val EXTENSION_TEX_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/tex2jax.js"
    val EXTENSION_TEX_FILE = "/extensions/tex2jax.js"

    val MATH_EVENTS_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/MathEvents.js"
    val MATH_EVENTS_FILE = "/extensions/MathEvents.js"

    val TEX_AMS_MATH_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/AMSmath.js"
    val TEX_AMS_MATH_FILE = "/extensions/TeX/AMSmath.js"

    val TEX_AMS_SYMBOL_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/AMSsymbols.js"
    val TEX_AMS_SYMBOL_FILE = "/extensions/TeX/AMSsymbols.js"

    val TEX_AUTOLOAD_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/autoload-all.js"
    val TEX_AUTOLOAD_FILE = "/extensions/TeX/autoload-all.js"

    val TEX_CANCEL_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/cancel.js"
    val TEX_CANCEL_FILE = "/extensions/TeX/cancel.js"

    val TEX_COLOR_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/color.js"
    val TEX_COLOR_FILE = "/extensions/TeX/color.js"

    val JAX_ELEMENT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/element/jax.js"
    val JAX_ELEMENT_FILE = "/jax/element/mml/jax.js"

    val JAX_INPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/input/jax.js"
    val JAX_INPUT_FILE = "/jax/input/TeX/jax.js"

    val CONFIG_INPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/input/config.js"
    val CONFIG_INPUT_FILE = "/jax/input/TeX/config.js"

    val MTABLE_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/mtable.js"
    val MTABLE_FILE = "/jax/output/HTML-CSS/autoload/mtable.js"

    val FONT_DATA_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/fontdata.js"
    val FONT_DATA_FILE = "/jax/output/HTML-CSS/fonts/STIX/fontdata.js"

    val FONT_DATA_1_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/fontdata-1.0.js"
    val FONT_DATA_1_FILE = "/jax/output/HTML-CSS/fonts/STIX/fontdata-1.0.js"

    val JAX_OUTPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/jax.js"
    val JAX_OUTPUT_FILE = "/jax/output/HTML-CSS/jax.js"

    val CONFIG_OUTPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/config.js"
    val CONFIG_OUTPUT_FILE = "/jax/output/HTML-CSS/config.js"

    val HINT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/hint.json"
    val HINT_JSON_FILE = "/hint.json"

    val ATTEMPT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/attempt.json"
    val ATTEMPT_JSON_FILE = "/attempt.json"

    val INTERNAL_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/internal-practice.json"
    val INTERNAL_FILE = "internal-practice.json"

    val MATERIAL_JS = "materialize.min.js"
    val MATERIAL_CSS = "materialize.min.css"

    val brainGenieLink = "braingenie.ck12.org"
    val slideShareLink = "www.slideshare.net"

    val ASB_CSS_HELPER = "/com/ustadmobile/lib/contentscrapers/asb/cssHelper.css"
    val PRATHAM_CSS_HELPER = "/com/ustadmobile/lib/contentscrapers/pratham/cssHelper.css"

    val MODULE_TIN_CAN_FILE = "http://adlnet.gov/expapi/activities/module"
    val SIMULATION_TIN_CAN_FILE = "http://adlnet.gov/expapi/activities/simulation"
    val VIDEO_TIN_CAN_FILE = "http://activitystrea.ms/schema/1.0/video"
    val ARTICLE_TIN_CAN_FILE = "http://activitystrea.ms/schema/1.0/ARTICLE_TIN_CAN_FILE"
    val ASSESMENT_TIN_CAN_FILE = "http://adlnet.gov/expapi/activities/assessment"

    val REQUEST_HEAD = "HEAD"

    val QUESTION_SET_HOLDER_TYPES = Arrays.asList(
            ComponentType.EXCERCISE.type, ComponentType.ONLINE.type,
            ComponentType.TEST.type, ComponentType.QUESTIONSET.type)


    val ARABIC_FONT_REGULAR = "DroidNaskh-Regular.woff2"
    val ARABIC_FONT_BOLD = "DroidNaskh-Bold.woiff2"
    val INDEX_HTML = "index.html"
    val JQUERY_JS = "jquery-3.3.1.min.js"

    val MIMETYPE_ZIP = "application/zip"
    val MIMETYPE_EPUB = "application/epub+zip"
    val MIMETYPE_JSON = "application/json"
    val MIMETYPE_SVG = "image/svg+xml"
    val MIMETYPE_JPG = "image/jpg"
    val MIMETYPE_WEBP = "image/webp"
    val MIMETYPE_CSS = "text/css"
    val MIMETYPE_MP4 = "video/mp4"
    val MIMETYPE_WEB_CHUNK = "application/webchunk+zip"
    val MIMETYPE_TINCAN = "application/tincan+zip"
    val MIMETYPE_WEBM = "video/webm"
    val MIMETYPE_KHAN = "application/khan-video+zip"

    val ZIP_EXT = ".zip"
    val PNG_EXT = ".png"
    val EPUB_EXT = ".epub"
    val SVG_EXT = ".svg"
    val JSON_EXT = ".json"
    val WEBP_EXT = ".webp"
    val JPEG_EXT = ".jpeg"
    val JPG_EXT = ".jpg"
    val MP4_EXT = ".mp4"
    val MP3_EXT = ".mp3"
    val WEBM_EXT = ".webm"
    val OPUS_EXT = ".opus"

    val IMAGE_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg")
    val VIDEO_EXTENSIONS = Arrays.asList("mp4")
    val AUDIO_EXTENSIONS = Arrays.asList("mp3")


    val VIDEO_FILENAME_MP4 = "video.mp4"
    val VIDEO_FILENAME_WEBM = "video.webm"
    val TINCAN_FILENAME = "tincan.xml"
    val SUBTITLE_FILENAME = "subtitle.srt"

    val ARABIC_LANG_CODE = "ar"
    val ENGLISH_LANG_CODE = "en"
    val USTAD_MOBILE = "Ustad Mobile"
    val ROOT = "root"
    val EMPTY_STRING = ""
    val EMPTY_SPACE = " "
    val FORWARD_SLASH = "/"
    val KHAN = "Khan Academy"

    val TIME_OUT_SELENIUM = 500


    enum class QUESTION_TYPE private constructor(val type: String) {

        MULTI_CHOICE("multiple-choice"),
        FILL_BLANKS("fill-in-the-blanks"),
        SHORT_ANSWER("short-answer")

    }


    enum class ComponentType private constructor(val type: String) {
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
        ONLINE("OnlineLesson")
    }

    enum class HtmlName(val type: String) {
        DESC("description"),
        FULL_DESC("full_description"),
        EXPLAIN("explaination"),
        CHOICE("choice"),
        HINT("hint")

    }

    enum class KhanContentType private constructor(val type: String) {
        TOPICS("Topics"),
        SUBJECT("Subjects"),
        VIDEO("Video"),
        EXERCISE("Exercise"),
        ARTICLE("Article")
    }

    enum class VoaContentType private constructor(val type: String) {
        LEVELS("Levels"),
        LESSONS("Lessons")
    }


    enum class CK12ContentType private constructor(val type: String) {
        SUBJECTS("Subjects")
    }


}
