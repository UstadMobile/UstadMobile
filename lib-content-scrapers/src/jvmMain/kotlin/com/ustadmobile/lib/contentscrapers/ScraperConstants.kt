package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.lib.db.entities.ContentEntry
import java.util.*

object ScraperConstants {

    const val CONTENT_JSON = "content.json"
    const val QUESTIONS_JSON = "questions.json"
    const val ETAG_TXT = "etag.txt"
    const val LAST_MODIFIED_TXT = "last-modified.txt"
    const val ABOUT_HTML = "about.txt"

    const val UTF_ENCODING = "UTF-8"
    const val EDRAAK_INDEX_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/edraak/index.html"
    const val CK12_INDEX_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/ck12/index.html"
    const val JS_TAG = "/com/ustadmobile/lib/contentscrapers/jquery-3.3.1.min.js"
    const val MATERIAL_JS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.js"
    const val MATERIAL_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.css"
    const val REGULAR_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/DroidNaskh-Regular.woff2"
    const val BOLD_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/DroidNaskh-Bold.woff2"
    const val CIRCULAR_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/css-circular-prog-bar.css"
    const val LANGUAGE_LIST_LOCATION = "/com/ustadmobile/lib/contentscrapers/iso_639_3.json"
    const val CIRCULAR_CSS_NAME = "css-circular-prog-bar.css"
    const val TIMER_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/timer.svg"
    const val TIMER_NAME = "timer.svg"
    const val TROPHY_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/trophy.svg"
    const val TROPHY_NAME = "trophy.svg"
    const val CHECK_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/check.svg"
    const val CHECK_NAME = "check.svg"
    const val XML_NAMESPACE = "http://purl.org/dc/elements/1.1/"

    const val KHAN_USERNAME = "no-reply@ustadmobile.com"
    const val KHAN_PASS = "ustadscraper"
    const val CK12_PASS = "ustadscraper1"
    const val KHAN_LOGIN_LINK = "https://pl.khanacademy.org/login"
    const val ANDROID_USER_AGENT = "user-agent=Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36"

    const val GRAPHIE = "+graphie"
    const val KHAN_GRAPHIE_PREFIX = "https://cdn.kastatic.org/ka-perseus-graphie/"


    const val EDRAAK_JS_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/edraak.min.js"
    const val EDRAAK_JS_FILENAME = "edraak.min.js"

    const val EDRAAK_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/edraak.min.css"
    const val EDRAAK_CSS_FILENAME = "edraak.min.css"

    const val VOA_JS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voa.min.js"
    const val VOA_JS_FILE_NAME = "voa.min.js"

    const val VOA_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voa.min.css"
    const val VOA_CSS_FILE_NAME = "voa.min.css"

    const val VOA_QUIZ_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voaquiz.min.css"
    const val VOA_QUIZ_CSS_FILE_NAME = "voaquiz.min.css"

    const val VOA_QUIZ_JS_LINK = "/com/ustadmobile/lib/contentscrapers/voa/voaquiz.min.js"
    const val VOA_QUIZ_JS_FILE_NAME = "voaquiz.min.js"

    const val CORRECT_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/exercise-correct.svg"
    const val CORRECT_FILE = "exercise-correct.svg"

    const val ATTEMPT_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/star-attempt.svg"
    const val ATTEMPT_FILE = "star-attempt.svg"

    const val COMPLETE_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/star-complete.svg"
    const val COMPLETE_FILE = "star-complete.svg"

    const val KHAN_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/khan/khanscraper.css"
    const val KHAN_CSS_FILE = "khanscraper.css"

    const val CC_LINK = "/com/ustadmobile/lib/contentscrapers/khan/cc.js"

    const val GENWEB_C9E_LINK  = "/com/ustadmobile/lib/contentscrapers/khan/genwebc9e.js"

    const val GENWEB_184_LINK  = "/com/ustadmobile/lib/contentscrapers/khan/genweb184.js"

    const val TRY_AGAIN_KHAN_LINK = "/com/ustadmobile/lib/contentscrapers/khan/exercise-try-again.svg"
    const val TRY_AGAIN_FILE = "exercise-try-again.svg"

    const val QUIZ_HTML_LINK = "/com/ustadmobile/lib/contentscrapers/voa/quiz.html"
    const val QUIZ_HTML_FILE = "quiz.html"

    const val IFRAME_RESIZE_LINK = "/com/ustadmobile/lib/contentscrapers/voa/iframeResizer.min.js"
    const val IFRAME_RESIZE_FILE = "iframeResizer.min.js"

    const val IFRAME_RESIZE_WINDOW_LINK = "/com/ustadmobile/lib/contentscrapers/voa/iframeResizer.contentWindow.min.js"
    const val IFRAME_RESIZE_WINDOW_FILE = "iframeResizer.contentWindow.min.js"

    // math jax links
    const val MATH_JAX_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/MathJax.js"
    const val MATH_JAX_FILE = "MathJax.js"

    const val JAX_CONFIG_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/TeX-AMS-MML_HTMLorMML.js"
    const val JAX_CONFIG_FILE = "/config/TeX-AMS-MML_HTMLorMML.js"

    const val EXTENSION_TEX_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/tex2jax.js"
    const val EXTENSION_TEX_FILE = "/extensions/tex2jax.js"

    const val MATH_EVENTS_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/MathEvents.js"
    const val MATH_EVENTS_FILE = "/extensions/MathEvents.js"

    const val TEX_AMS_MATH_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/AMSmath.js"
    const val TEX_AMS_MATH_FILE = "/extensions/TeX/AMSmath.js"

    const val TEX_AMS_SYMBOL_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/AMSsymbols.js"
    const val TEX_AMS_SYMBOL_FILE = "/extensions/TeX/AMSsymbols.js"

    const val TEX_AUTOLOAD_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/autoload-all.js"
    const val TEX_AUTOLOAD_FILE = "/extensions/TeX/autoload-all.js"

    const val TEX_CANCEL_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/cancel.js"
    const val TEX_CANCEL_FILE = "/extensions/TeX/cancel.js"

    const val TEX_COLOR_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/color.js"
    const val TEX_COLOR_FILE = "/extensions/TeX/color.js"

    const val JAX_ELEMENT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/element/jax.js"
    const val JAX_ELEMENT_FILE = "/jax/element/mml/jax.js"

    const val JAX_INPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/input/jax.js"
    const val JAX_INPUT_FILE = "/jax/input/TeX/jax.js"

    const val CONFIG_INPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/input/config.js"
    const val CONFIG_INPUT_FILE = "/jax/input/TeX/config.js"

    const val MTABLE_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/mtable.js"
    const val MTABLE_FILE = "/jax/output/HTML-CSS/autoload/mtable.js"

    const val FONT_DATA_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/fontdata.js"
    const val FONT_DATA_FILE = "/jax/output/HTML-CSS/fonts/STIX/fontdata.js"

    const val FONT_DATA_1_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/fontdata-1.0.js"
    const val FONT_DATA_1_FILE = "/jax/output/HTML-CSS/fonts/STIX/fontdata-1.0.js"

    const val JAX_OUTPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/jax.js"
    const val JAX_OUTPUT_FILE = "/jax/output/HTML-CSS/jax.js"

    const val CONFIG_OUTPUT_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/mathjax/output/config.js"
    const val CONFIG_OUTPUT_FILE = "/jax/output/HTML-CSS/config.js"

    const val HINT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/hint.json"
    const val HINT_JSON_FILE = "/hint.json"

    const val ATTEMPT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/attempt.json"
    const val ATTEMPT_JSON_FILE = "/attempt.json"

    const val INTERNAL_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/internal-practice.json"
    const val INTERNAL_FILE = "internal-practice.json"

    const val MATERIAL_JS = "materialize.min.js"
    const val MATERIAL_CSS = "materialize.min.css"

    const val brainGenieLink = "braingenie.ck12.org"
    const val slideShareLink = "www.slideshare.net"

    const val ASB_CSS_HELPER = "/com/ustadmobile/lib/contentscrapers/asb/cssHelper.css"
    const val PRATHAM_CSS_HELPER = "/com/ustadmobile/lib/contentscrapers/pratham/cssHelper.css"

    const val MODULE_TIN_CAN_FILE = "http://adlnet.gov/expapi/activities/module"
    const val SIMULATION_TIN_CAN_FILE = "http://adlnet.gov/expapi/activities/simulation"
    const val VIDEO_TIN_CAN_FILE = "http://activitystrea.ms/schema/1.0/video"
    const val ARTICLE_TIN_CAN_FILE = "http://activitystrea.ms/schema/1.0/ARTICLE_TIN_CAN_FILE"
    const val ASSESMENT_TIN_CAN_FILE = "http://adlnet.gov/expapi/activities/assessment"

    const val REQUEST_HEAD = "HEAD"

    val QUESTION_SET_HOLDER_TYPES = Arrays.asList(
            ComponentType.EXCERCISE.type, ComponentType.ONLINE.type,
            ComponentType.TEST.type, ComponentType.QUESTIONSET.type)


    const val ARABIC_FONT_REGULAR = "DroidNaskh-Regular.woff2"
    const val ARABIC_FONT_BOLD = "DroidNaskh-Bold.woiff2"
    const val INDEX_HTML = "index.html"
    const val JQUERY_JS = "jquery-3.3.1.min.js"

    const val MIMETYPE_ZIP = "application/zip"
    const val MIMETYPE_EPUB = "application/epub+zip"
    const val MIMETYPE_JSON = "application/json"
    const val MIMETYPE_SVG = "image/svg+xml"
    const val MIMETYPE_JPG = "image/jpg"
    const val MIMETYPE_WEBP = "image/webp"
    const val MIMETYPE_CSS = "text/css"
    const val MIMETYPE_JS = "application/javascript"
    const val MIMETYPE_MP4 = "video/mp4"
    const val MIMETYPE_WEB_CHUNK = "application/webchunk+zip"
    const val MIMETYPE_HAR = "application/har+zip"
    const val MIMETYPE_TINCAN = "application/tincan+zip"
    const val MIMETYPE_WEBM = "video/webm"
    const val MIMETYPE_KHAN = "application/khan-video+zip"
    const val MIMETYPE_PDF = "application/pdf"

    const val ZIP_EXT = ".zip"
    const val PNG_EXT = ".png"
    const val EPUB_EXT = ".epub"
    const val SVG_EXT = ".svg"
    const val JSON_EXT = ".json"
    const val WEBP_EXT = ".webp"
    const val JPEG_EXT = ".jpeg"
    const val JPG_EXT = ".jpg"
    const val MP4_EXT = ".mp4"
    const val MP3_EXT = ".mp3"
    const val WEBM_EXT = ".webm"
    const val OPUS_EXT = ".opus"

    const val CK12_VIDEO = "video"
    const val CK12_PLIX = "plix"
    const val CK12_PRACTICE = "practice"
    const val CK12_READ = "read"
    const val CK12_ACTIVITIES = "activities"
    const val CK12_STUDY_AIDS = "study aids"
    const val CK12_LESSONS = "lesson plans"
    const val CK12_READ_WORLD = "real world"

    val CONTENT_MAP_CK12 = mapOf(
            CK12_VIDEO to ContentEntry.VIDEO_TYPE,
            CK12_PLIX to ContentEntry.INTERACTIVE_EXERICSE_TYPE,
            CK12_PRACTICE to ContentEntry.INTERACTIVE_EXERICSE_TYPE,
            CK12_READ to ContentEntry.ARTICLE_TYPE,
            CK12_ACTIVITIES to ContentEntry.ARTICLE_TYPE,
            CK12_STUDY_AIDS to ContentEntry.ARTICLE_TYPE,
            CK12_LESSONS to ContentEntry.ARTICLE_TYPE,
            CK12_READ_WORLD to ContentEntry.ARTICLE_TYPE)

    val IMAGE_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg")
    val VIDEO_EXTENSIONS = Arrays.asList("mp4")
    val AUDIO_EXTENSIONS = Arrays.asList("mp3")

    const val VIDEO_FILENAME_MP4 = "video.mp4"
    const val VIDEO_FILENAME_WEBM = "video.webm"
    const val TINCAN_FILENAME = "tincan.xml"
    const val SUBTITLE_FILENAME = "subtitle.srt"

    const val ARABIC_LANG_CODE = "ar"
    const val ENGLISH_LANG_CODE = "en"
    const val USTAD_MOBILE = "Ustad Mobile"
    const val ROOT = "root"
    const val EMPTY_STRING = ""
    const val EMPTY_SPACE = " "
    const val FORWARD_SLASH = "/"
    const val KHAN = "Khan Academy"
    const val GDL = "Global Digital Library"
    const val CK12 = "CK12"
    const val HAB = "Habaybna"

    const val TIME_OUT_SELENIUM = 120


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

    enum class GDLContentType constructor(val type: String){
        ROOT("ROOT"),
        ENTRY("ENTRY"),
        LANGPAGE("LANG_PAGE"),
        CONTENT("CONTENT")
    }

    enum class VoaContentType private constructor(val type: String) {
        LEVELS("Levels"),
        LESSONS("Lessons")
    }


    enum class CK12ContentType private constructor(val type: String) {
        ROOT("ROOT"),
        SUBJECTS("Subjects"),
        GRADES("GRADES"),
        CONTENT("CONTENT")
    }


}
