package com.ustadmobile.lib.contentscrapers

import com.soywiz.klock.DateFormat
import com.ustadmobile.lib.db.entities.ContentEntry
import java.time.Duration
import java.util.*

object ScraperConstants {

    const val SCRAPER_TAG = "Scraper"

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
    const val CIRCULAR_CSS_NAME = "css-circular-prog-bar.css"
    const val TIMER_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/timer.svg"
    const val TIMER_NAME = "timer.svg"
    const val TROPHY_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/trophy.svg"
    const val TROPHY_NAME = "trophy.svg"
    const val CHECK_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/check.svg"
    const val CHECK_NAME = "check.svg"
    const val XML_NAMESPACE = "http://purl.org/dc/elements/1.1/"
    const val POST_METHOD = "POST"

    const val KHAN_USERNAME = "no-reply@ustadmobile.com"
    const val KHAN_PASS = "ustadscraper"
    const val CK12_PASS = "ustadscraper1"
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

    const val KHAN_TAKE_HINT_LINK = "/com/ustadmobile/lib/contentscrapers/khan/take-a-hint.txt"

    const val GENWEB_ANSWER_LINK  = "/com/ustadmobile/lib/contentscrapers/khan/genwebanswer.js"


    const val END_OF_TASK_AUDIO = "/com/ustadmobile/lib/contentscrapers/khan/end-of-task.ogg"
    const val LATO_LATIN_BOLD_WOFF = "/com/ustadmobile/lib/contentscrapers/khan/LatoLatin-Bold.woff2"
    const val LATO_LATIN_REGULAR_WOFF = "/com/ustadmobile/lib/contentscrapers/khan/LatoLatin-Regular.woff2"
    const val LATO_LATIN_ITALITC_WOFF = "/com/ustadmobile/lib/contentscrapers/khan/LatoLatin-Italic.woff2"
    const val NOTO_BOLD_WOFF = "/com/ustadmobile/lib/contentscrapers/khan/NotoSansArmenian-Bold.woff2"
    const val NOTO_REGULAR_WOFF = "/com/ustadmobile/lib/contentscrapers/khan/NotoSansArmenian-Regular.woff2"
    const val MATH_JAX_4_REG_WOFF = "/com/ustadmobile/lib/contentscrapers/khan/MathJax_Size4-Regular.woff"
    const val MATH_JAX_4_REG_OTF = "/com/ustadmobile/lib/contentscrapers/khan/MathJax_Size4-Regular.otf"

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


    const val HINT_JSON_FILE = "/hint.json"
    const val HINT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/hint.json"
    const val PL_HINT_JSON_LINK = "{\"streak\": 0, \"isSkillCheck\": false, \"snoozeTime\": null, \"secondsPerFastProblem\": 4.0, \"exerciseStates\": {\"struggling\": false, \"proficient\": false, \"practiced\": false, \"mastered\": false}, \"practicedDate\": null, \"lastDone\": \"2020-03-10T08:04:44Z\", \"maximumExerciseProgress\": {\"practiced\": false, \"mastered\": false, \"level\": \"unstarted\"}, \"lastCountHints\": 0, \"updatedRecommendations\": null, \"longestStreak\": 1, \"proficientDate\": null, \"MASTERY_CARD_SUPERPROMOTE_THRESHOLD\": 0.85, \"struggling\": false, \"lastAttemptNumber\": 2, \"exercise\": \"counting-out-1-20-objects\", \"totalDone\": 1, \"maximumExerciseProgressDt\": null, \"lastMasteryUpdate\": null, \"exerciseModel\": {\"imageUrl_256\": \"https://cdn.kastatic.org/ka-exercise-screenshots/counting-out-1-20-objects_256.png\", \"isSkillCheck\": false, \"translatedPrettyDisplayName\": \"Liczenie z ma\\u0142ymi liczbami\", \"customDescriptionTag\": \"\", \"translatedCustomTitleTag\": \"\", \"tutorialOnly\": false, \"thumbnailData\": {\"url\": \"https://lh3.googleusercontent.com/OT67_6ynI74UbYovMNfp1ep31gh8JIzRseLWc8cQcgruTkJsoALAVMTG1bah5HO8tl1cMrcuI3If2qpIKYw_PBo\", \"skipFilter\": false, \"titleText\": \"\", \"gcsName\": \"/gs/ka_thumbnails/L2FwcGhvc3RpbmdfcHJvZC9ibG9icy9BRW5CMlVxUzBSR0FCS1piRExKdkk4VmlZVkhKTzVLMFBrVEVyZEh2eUJTUzduT21iaDVYcV95WEFqdHpKMFFzQ2hDRC1LakMtb1diZGhiWU9lNWpPQzhpNkY0bGNXaElkdy42djNfTGZRaEFudlAzd0tp\"}, \"relativeUrl\": \"/exercise/counting-out-1-20-objects\", \"difficultyLevel\": null, \"allAssessmentItems\": [{\"sha\": \"83a66d7bb11feb7d4c989aed6a475a009ec73dd9\", \"live\": true, \"id\": \"xde8147b8edb82294\"}, {\"sha\": \"76c7fd55c20dfe0f4f938745b73e1e7d0c4757ac\", \"live\": true, \"id\": \"xa5c8d62485b6bf16\"}, {\"sha\": \"a4318a5630a8766f55dc6830527dbaffc7fb917b\", \"live\": true, \"id\": \"x2313c50d4dfd4a0a\"}, {\"sha\": \"b350b21cf286a64c0e7869a6429fc6c5fd139c1f\", \"live\": true, \"id\": \"xcd7ba1c0c381fadb\"}, {\"sha\": \"e8ba59f1f99433fbd0360332f999cde76ab8976b\", \"live\": true, \"id\": \"x4cb56360820eece5\"}, {\"sha\": \"954fcd5885c36b841a22bc5bf89207bced27b8be\", \"live\": true, \"id\": \"xcc39b61282c884be\"}, {\"sha\": \"15070fc64826e1cd00665c38e2e42e5834ed359a\", \"live\": true, \"id\": \"x9c6c9733676e5240\"}, {\"sha\": \"89d992ba940c4ecaa7687d7a84ce632ead085882\", \"live\": true, \"id\": \"x8ae458b86dfe440b\"}, {\"sha\": \"54f92d19cabbd7647cd5d21d4678e8711431e9fb\", \"live\": true, \"id\": \"x8e7fd4a4b5b002c1\"}, {\"sha\": \"8d00285eae0fab5210dcfb325be7741e48c75681\", \"live\": true, \"id\": \"xeee62e178c0cfe6f\"}, {\"sha\": \"4453b349bad32a1e416e7e74af64e09e2c5810f8\", \"live\": true, \"id\": \"xdee0840c85c0add5\"}, {\"sha\": \"bb340ca13b104a9b9d1065b74a2fdfa423f817d1\", \"live\": true, \"id\": \"xa756d02df7435e1a\"}, {\"sha\": \"f65a9c4a6a8df260021fe424d562e8ff6d1741bd\", \"live\": true, \"id\": \"x6b9db70231ff254d\"}, {\"sha\": \"00808cbc066910be1220235faa7d93a9126f6a57\", \"live\": true, \"id\": \"x1855395b96b1e34f\"}, {\"sha\": \"90db973ee0fd319c596410e215a76a651601901d\", \"live\": true, \"id\": \"x477b6212a24d08da\"}, {\"sha\": \"f6cf4d0675de30afee8cab929b97000e2c41fbc8\", \"live\": true, \"id\": \"xfc8946e7c80f2800\"}, {\"sha\": \"4f522fb965ab3805f56250db187fefadb281edd2\", \"live\": true, \"id\": \"x14784d8f428fa949\"}, {\"sha\": \"849c88aa2122ea5a40bb39540acf551d3ac67728\", \"live\": true, \"id\": \"x3f2d3b6cb53f67a4\"}, {\"sha\": \"a9197ffb73ad014d477c7eb1eed41f9ea025774b\", \"live\": true, \"id\": \"xb6e923d2f396f5ab\"}, {\"sha\": \"446eef76c02fc29049012312c704c12b4b54ba8d\", \"live\": true, \"id\": \"x9ef4ca7e914c87ec\"}], \"customTitleTag\": \"\", \"covers\": [], \"currentRevisionKey\": \"fe136c29440e7aa557a02674acdb12353d82ee63\", \"trackingDocumentUrl\": null, \"hasCustomThumbnail\": false, \"translatedCustomDescriptionTag\": \"\", \"alternateSlugs\": [], \"translatedShortDisplayName\": \"Liczenie z \", \"conceptTagsInfo\": [{\"displayName\": \"Liczenie\", \"id\": \"Tag:x731f236a6d33f0ff\", \"slug\": \"counting\"}, {\"displayName\": \"Liczenie przedmiot\\u00f3w\", \"id\": \"Tag:xf444fba1e72e5158\", \"slug\": \"counting-objects\"}], \"id\": \"x4debd8a3\", \"description\": \"Practice counting up to 10 objects.\", \"summative\": false, \"importedFromSha\": null, \"doNotPublish\": false, \"hide\": false, \"secondsPerFastProblem\": 4.0, \"vPosition\": 3, \"authorKey\": \"ag5zfmtoYW4tYWNhZGVteXJaCxIIVXNlckRhdGEiTHVzZXJfaWRfa2V5X2h0dHA6Ly9ub3VzZXJpZC5raGFuYWNhZGVteS5vcmcvMzJhNTI4ZjZmMjVkMGI2OTFjYWNkMDVkNzk2ZTExYmMM\", \"live\": true, \"conceptTags\": [\"Tag:x731f236a6d33f0ff\", \"Tag:xf444fba1e72e5158\"], \"authorName\": \"Gail Hargrave\", \"assessmentItemTags\": [\"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICAwIKTqAoM\", \"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICAgKqcsgoM\", \"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICA4KOStgoM\", \"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICA4KP9oAoM\"], \"deletedModTime\": null, \"descriptionHtml\": \"Practice counting up to 10 objects.\", \"progressKey\": \"ex4debd8a3\", \"globalId\": \"ex4debd8a3\", \"usesAssessmentItems\": true, \"contentKind\": \"Exercise\", \"dateModified\": \"2020-01-06T21:50:52Z\", \"listed\": true, \"problemTypes\": [{\"relatedVideos\": [], \"items\": [{\"sha\": \"83a66d7bb11feb7d4c989aed6a475a009ec73dd9\", \"live\": true, \"id\": \"xde8147b8edb82294\"}, {\"sha\": \"76c7fd55c20dfe0f4f938745b73e1e7d0c4757ac\", \"live\": true, \"id\": \"xa5c8d62485b6bf16\"}, {\"sha\": \"a4318a5630a8766f55dc6830527dbaffc7fb917b\", \"live\": true, \"id\": \"x2313c50d4dfd4a0a\"}, {\"sha\": \"b350b21cf286a64c0e7869a6429fc6c5fd139c1f\", \"live\": true, \"id\": \"xcd7ba1c0c381fadb\"}, {\"sha\": \"e8ba59f1f99433fbd0360332f999cde76ab8976b\", \"live\": true, \"id\": \"x4cb56360820eece5\"}, {\"sha\": \"954fcd5885c36b841a22bc5bf89207bced27b8be\", \"live\": true, \"id\": \"xcc39b61282c884be\"}, {\"sha\": \"15070fc64826e1cd00665c38e2e42e5834ed359a\", \"live\": true, \"id\": \"x9c6c9733676e5240\"}, {\"sha\": \"89d992ba940c4ecaa7687d7a84ce632ead085882\", \"live\": true, \"id\": \"x8ae458b86dfe440b\"}, {\"sha\": \"54f92d19cabbd7647cd5d21d4678e8711431e9fb\", \"live\": true, \"id\": \"x8e7fd4a4b5b002c1\"}, {\"sha\": \"8d00285eae0fab5210dcfb325be7741e48c75681\", \"live\": true, \"id\": \"xeee62e178c0cfe6f\"}, {\"sha\": \"4453b349bad32a1e416e7e74af64e09e2c5810f8\", \"live\": true, \"id\": \"xdee0840c85c0add5\"}, {\"sha\": \"bb340ca13b104a9b9d1065b74a2fdfa423f817d1\", \"live\": true, \"id\": \"xa756d02df7435e1a\"}, {\"sha\": \"f65a9c4a6a8df260021fe424d562e8ff6d1741bd\", \"live\": true, \"id\": \"x6b9db70231ff254d\"}, {\"sha\": \"00808cbc066910be1220235faa7d93a9126f6a57\", \"live\": true, \"id\": \"x1855395b96b1e34f\"}, {\"sha\": \"90db973ee0fd319c596410e215a76a651601901d\", \"live\": true, \"id\": \"x477b6212a24d08da\"}, {\"sha\": \"f6cf4d0675de30afee8cab929b97000e2c41fbc8\", \"live\": true, \"id\": \"xfc8946e7c80f2800\"}, {\"sha\": \"4f522fb965ab3805f56250db187fefadb281edd2\", \"live\": true, \"id\": \"x14784d8f428fa949\"}, {\"sha\": \"849c88aa2122ea5a40bb39540acf551d3ac67728\", \"live\": true, \"id\": \"x3f2d3b6cb53f67a4\"}, {\"sha\": \"a9197ffb73ad014d477c7eb1eed41f9ea025774b\", \"live\": true, \"id\": \"xb6e923d2f396f5ab\"}, {\"sha\": \"446eef76c02fc29049012312c704c12b4b54ba8d\", \"live\": true, \"id\": \"x9ef4ca7e914c87ec\"}], \"name\": \"A\"}], \"translatedDisplayName\": \"Liczenie z ma\\u0142ymi liczbami\", \"creationDate\": \"2018-02-26T22:54:43Z\", \"editSlug\": \"edit/e/x4debd8a3\", \"sourceLanguage\": \"en\", \"deleted\": false, \"usesWorkedExamples\": false, \"translatedDescription\": \"\\u0106wiczenie w liczeniu do 10 obiekt\\u00f3w.\", \"fileName\": null, \"translatedProblemTypes\": [{\"relatedVideos\": [], \"items\": [{\"sha\": \"83a66d7bb11feb7d4c989aed6a475a009ec73dd9\", \"live\": true, \"id\": \"xde8147b8edb82294\"}, {\"sha\": \"76c7fd55c20dfe0f4f938745b73e1e7d0c4757ac\", \"live\": true, \"id\": \"xa5c8d62485b6bf16\"}, {\"sha\": \"a4318a5630a8766f55dc6830527dbaffc7fb917b\", \"live\": true, \"id\": \"x2313c50d4dfd4a0a\"}, {\"sha\": \"b350b21cf286a64c0e7869a6429fc6c5fd139c1f\", \"live\": true, \"id\": \"xcd7ba1c0c381fadb\"}, {\"sha\": \"e8ba59f1f99433fbd0360332f999cde76ab8976b\", \"live\": true, \"id\": \"x4cb56360820eece5\"}, {\"sha\": \"954fcd5885c36b841a22bc5bf89207bced27b8be\", \"live\": true, \"id\": \"xcc39b61282c884be\"}, {\"sha\": \"15070fc64826e1cd00665c38e2e42e5834ed359a\", \"live\": true, \"id\": \"x9c6c9733676e5240\"}, {\"sha\": \"89d992ba940c4ecaa7687d7a84ce632ead085882\", \"live\": true, \"id\": \"x8ae458b86dfe440b\"}, {\"sha\": \"54f92d19cabbd7647cd5d21d4678e8711431e9fb\", \"live\": true, \"id\": \"x8e7fd4a4b5b002c1\"}, {\"sha\": \"8d00285eae0fab5210dcfb325be7741e48c75681\", \"live\": true, \"id\": \"xeee62e178c0cfe6f\"}, {\"sha\": \"4453b349bad32a1e416e7e74af64e09e2c5810f8\", \"live\": true, \"id\": \"xdee0840c85c0add5\"}, {\"sha\": \"bb340ca13b104a9b9d1065b74a2fdfa423f817d1\", \"live\": true, \"id\": \"xa756d02df7435e1a\"}, {\"sha\": \"f65a9c4a6a8df260021fe424d562e8ff6d1741bd\", \"live\": true, \"id\": \"x6b9db70231ff254d\"}, {\"sha\": \"00808cbc066910be1220235faa7d93a9126f6a57\", \"live\": true, \"id\": \"x1855395b96b1e34f\"}, {\"sha\": \"90db973ee0fd319c596410e215a76a651601901d\", \"live\": true, \"id\": \"x477b6212a24d08da\"}, {\"sha\": \"f6cf4d0675de30afee8cab929b97000e2c41fbc8\", \"live\": true, \"id\": \"xfc8946e7c80f2800\"}, {\"sha\": \"4f522fb965ab3805f56250db187fefadb281edd2\", \"live\": true, \"id\": \"x14784d8f428fa949\"}, {\"sha\": \"849c88aa2122ea5a40bb39540acf551d3ac67728\", \"live\": true, \"id\": \"x3f2d3b6cb53f67a4\"}, {\"sha\": \"a9197ffb73ad014d477c7eb1eed41f9ea025774b\", \"live\": true, \"id\": \"xb6e923d2f396f5ab\"}, {\"sha\": \"446eef76c02fc29049012312c704c12b4b54ba8d\", \"live\": true, \"id\": \"x9ef4ca7e914c87ec\"}], \"name\": \"A\"}], \"authorList\": [], \"sponsored\": false, \"isQuiz\": false, \"shortDisplayName\": \"Count with \", \"curatedRelatedVideos\": [\"x9b4a5e7a\"], \"nodeSlug\": \"e/counting-out-1-20-objects\", \"tags\": [], \"hPosition\": -40, \"authorNames\": [], \"kind\": \"Exercise\", \"sha1\": \"4958b98468177d530f2a556a5f1cd0a038c74d1a\", \"displayName\": \"Count with small numbers\", \"name\": \"counting-out-1-20-objects\", \"translatedDescriptionHtml\": \"\\u0106wiczenie w liczeniu do 10 obiekt\\u00f3w.\", \"prerequisites\": [], \"contentId\": \"x4debd8a3\", \"relatedContent\": [\"video:x9b4a5e7a\"], \"title\": \"Count with small numbers\", \"backupTimestamp\": null, \"kaUrl\": \"https://pl.khanacademy.org/exercise/counting-out-1-20-objects\", \"suggestedCompletionCriteria\": \"num_problems_7\", \"sha\": \"fe136c29440e7aa557a02674acdb12353d82ee63\", \"prettyDisplayName\": \"Count with small numbers\", \"imageUrl\": \"https://cdn.kastatic.org/ka-exercise-screenshots/counting-out-1-20-objects.png\", \"translatedTitle\": \"Liczenie z ma\\u0142ymi liczbami\"}, \"totalCorrect\": 1, \"contentKind\": \"UserExercise\", \"exerciseProgress\": {\"practiced\": false, \"mastered\": false, \"level\": \"unstarted\"}, \"promotions\": {}, \"kind\": \"UserExercise\", \"practiced\": false, \"masteryPoints\": 0, \"clearStrugglingIndicators\": false, \"backupTimestamp\": \"2020-03-10T08:04:02Z\", \"mastered\": false, \"fpmMasteryLevel\": \"unfamiliar\", \"kaid\": \"kaid_1111324816458145736916590\", \"actionResults\": {\"notificationsAdded\": {\"toast\": [], \"readable\": [], \"urgent\": [], \"avatarParts\": [], \"continueUrl\": \"https://pl.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-out-1-20-objects\", \"badges\": []}, \"badgeCounts\": {\"0\": 0, \"1\": 0, \"2\": 0, \"3\": 0, \"4\": 0, \"5\": 0}, \"pointsEarned\": {\"points\": 0}, \"userProfile\": {\"countVideosCompleted\": 0, \"countBrandNewNotifications\": 0, \"streakLastLength\": 1, \"streakLength\": 1, \"points\": 150, \"streakLastExtended\": \"2020-03-10\"}}}"
    const val HY_HINT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/hy/hint.json"


    const val ATTEMPT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/attempt.json"
    const val ATTEMPT_PROBLEM_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/attemptProblem.json"
    const val ATTEMPT_JSON_FILE = "/attempt.json"

    const val PL_ATTEMPT_JSON_LINK = "{\"streak\": 0, \"isSkillCheck\": false, \"snoozeTime\": null, \"secondsPerFastProblem\": 4.0, \"exerciseStates\": {\"struggling\": false, \"proficient\": false, \"practiced\": false, \"mastered\": false}, \"practicedDate\": null, \"lastDone\": \"2020-03-10T08:04:02Z\", \"maximumExerciseProgress\": {\"practiced\": false, \"mastered\": false, \"level\": \"unstarted\"}, \"updatedRecommendations\": null, \"longestStreak\": 1, \"lastCountHints\": 0, \"MASTERY_CARD_SUPERPROMOTE_THRESHOLD\": 0.85, \"struggling\": false, \"lastAttemptNumber\": 1, \"exercise\": \"counting-out-1-20-objects\", \"totalDone\": 1, \"maximumExerciseProgressDt\": null, \"lastMasteryUpdate\": null, \"exerciseModel\": {\"imageUrl_256\": \"https://cdn.kastatic.org/ka-exercise-screenshots/counting-out-1-20-objects_256.png\", \"isSkillCheck\": false, \"translatedPrettyDisplayName\": \"Liczenie z ma\\u0142ymi liczbami\", \"customDescriptionTag\": \"\", \"translatedCustomTitleTag\": \"\", \"tutorialOnly\": false, \"thumbnailData\": {\"url\": \"https://lh3.googleusercontent.com/OT67_6ynI74UbYovMNfp1ep31gh8JIzRseLWc8cQcgruTkJsoALAVMTG1bah5HO8tl1cMrcuI3If2qpIKYw_PBo\", \"skipFilter\": false, \"titleText\": \"\", \"gcsName\": \"/gs/ka_thumbnails/L2FwcGhvc3RpbmdfcHJvZC9ibG9icy9BRW5CMlVxUzBSR0FCS1piRExKdkk4VmlZVkhKTzVLMFBrVEVyZEh2eUJTUzduT21iaDVYcV95WEFqdHpKMFFzQ2hDRC1LakMtb1diZGhiWU9lNWpPQzhpNkY0bGNXaElkdy42djNfTGZRaEFudlAzd0tp\"}, \"relativeUrl\": \"/exercise/counting-out-1-20-objects\", \"difficultyLevel\": null, \"allAssessmentItems\": [{\"sha\": \"83a66d7bb11feb7d4c989aed6a475a009ec73dd9\", \"live\": true, \"id\": \"xde8147b8edb82294\"}, {\"sha\": \"76c7fd55c20dfe0f4f938745b73e1e7d0c4757ac\", \"live\": true, \"id\": \"xa5c8d62485b6bf16\"}, {\"sha\": \"a4318a5630a8766f55dc6830527dbaffc7fb917b\", \"live\": true, \"id\": \"x2313c50d4dfd4a0a\"}, {\"sha\": \"b350b21cf286a64c0e7869a6429fc6c5fd139c1f\", \"live\": true, \"id\": \"xcd7ba1c0c381fadb\"}, {\"sha\": \"e8ba59f1f99433fbd0360332f999cde76ab8976b\", \"live\": true, \"id\": \"x4cb56360820eece5\"}, {\"sha\": \"954fcd5885c36b841a22bc5bf89207bced27b8be\", \"live\": true, \"id\": \"xcc39b61282c884be\"}, {\"sha\": \"15070fc64826e1cd00665c38e2e42e5834ed359a\", \"live\": true, \"id\": \"x9c6c9733676e5240\"}, {\"sha\": \"89d992ba940c4ecaa7687d7a84ce632ead085882\", \"live\": true, \"id\": \"x8ae458b86dfe440b\"}, {\"sha\": \"54f92d19cabbd7647cd5d21d4678e8711431e9fb\", \"live\": true, \"id\": \"x8e7fd4a4b5b002c1\"}, {\"sha\": \"8d00285eae0fab5210dcfb325be7741e48c75681\", \"live\": true, \"id\": \"xeee62e178c0cfe6f\"}, {\"sha\": \"4453b349bad32a1e416e7e74af64e09e2c5810f8\", \"live\": true, \"id\": \"xdee0840c85c0add5\"}, {\"sha\": \"bb340ca13b104a9b9d1065b74a2fdfa423f817d1\", \"live\": true, \"id\": \"xa756d02df7435e1a\"}, {\"sha\": \"f65a9c4a6a8df260021fe424d562e8ff6d1741bd\", \"live\": true, \"id\": \"x6b9db70231ff254d\"}, {\"sha\": \"00808cbc066910be1220235faa7d93a9126f6a57\", \"live\": true, \"id\": \"x1855395b96b1e34f\"}, {\"sha\": \"90db973ee0fd319c596410e215a76a651601901d\", \"live\": true, \"id\": \"x477b6212a24d08da\"}, {\"sha\": \"f6cf4d0675de30afee8cab929b97000e2c41fbc8\", \"live\": true, \"id\": \"xfc8946e7c80f2800\"}, {\"sha\": \"4f522fb965ab3805f56250db187fefadb281edd2\", \"live\": true, \"id\": \"x14784d8f428fa949\"}, {\"sha\": \"849c88aa2122ea5a40bb39540acf551d3ac67728\", \"live\": true, \"id\": \"x3f2d3b6cb53f67a4\"}, {\"sha\": \"a9197ffb73ad014d477c7eb1eed41f9ea025774b\", \"live\": true, \"id\": \"xb6e923d2f396f5ab\"}, {\"sha\": \"446eef76c02fc29049012312c704c12b4b54ba8d\", \"live\": true, \"id\": \"x9ef4ca7e914c87ec\"}], \"customTitleTag\": \"\", \"covers\": [], \"currentRevisionKey\": \"fe136c29440e7aa557a02674acdb12353d82ee63\", \"trackingDocumentUrl\": null, \"hasCustomThumbnail\": false, \"translatedCustomDescriptionTag\": \"\", \"alternateSlugs\": [], \"translatedShortDisplayName\": \"Liczenie z \", \"conceptTagsInfo\": [{\"displayName\": \"Liczenie\", \"id\": \"Tag:x731f236a6d33f0ff\", \"slug\": \"counting\"}, {\"displayName\": \"Liczenie przedmiot\\u00f3w\", \"id\": \"Tag:xf444fba1e72e5158\", \"slug\": \"counting-objects\"}], \"id\": \"x4debd8a3\", \"description\": \"Practice counting up to 10 objects.\", \"summative\": false, \"importedFromSha\": null, \"doNotPublish\": false, \"hide\": false, \"secondsPerFastProblem\": 4.0, \"vPosition\": 3, \"authorKey\": \"ag5zfmtoYW4tYWNhZGVteXJaCxIIVXNlckRhdGEiTHVzZXJfaWRfa2V5X2h0dHA6Ly9ub3VzZXJpZC5raGFuYWNhZGVteS5vcmcvMzJhNTI4ZjZmMjVkMGI2OTFjYWNkMDVkNzk2ZTExYmMM\", \"live\": true, \"conceptTags\": [\"Tag:x731f236a6d33f0ff\", \"Tag:xf444fba1e72e5158\"], \"authorName\": \"Gail Hargrave\", \"assessmentItemTags\": [\"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICAwIKTqAoM\", \"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICAgKqcsgoM\", \"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICA4KOStgoM\", \"ag5zfmtoYW4tYWNhZGVteXI2CxIRQXNzZXNzbWVudEl0ZW1UYWciATAMCxIRQXNzZXNzbWVudEl0ZW1UYWcYgICA4KP9oAoM\"], \"deletedModTime\": null, \"descriptionHtml\": \"Practice counting up to 10 objects.\", \"progressKey\": \"ex4debd8a3\", \"globalId\": \"ex4debd8a3\", \"usesAssessmentItems\": true, \"contentKind\": \"Exercise\", \"dateModified\": \"2020-01-06T21:50:52Z\", \"listed\": true, \"problemTypes\": [{\"relatedVideos\": [], \"items\": [{\"sha\": \"83a66d7bb11feb7d4c989aed6a475a009ec73dd9\", \"live\": true, \"id\": \"xde8147b8edb82294\"}, {\"sha\": \"76c7fd55c20dfe0f4f938745b73e1e7d0c4757ac\", \"live\": true, \"id\": \"xa5c8d62485b6bf16\"}, {\"sha\": \"a4318a5630a8766f55dc6830527dbaffc7fb917b\", \"live\": true, \"id\": \"x2313c50d4dfd4a0a\"}, {\"sha\": \"b350b21cf286a64c0e7869a6429fc6c5fd139c1f\", \"live\": true, \"id\": \"xcd7ba1c0c381fadb\"}, {\"sha\": \"e8ba59f1f99433fbd0360332f999cde76ab8976b\", \"live\": true, \"id\": \"x4cb56360820eece5\"}, {\"sha\": \"954fcd5885c36b841a22bc5bf89207bced27b8be\", \"live\": true, \"id\": \"xcc39b61282c884be\"}, {\"sha\": \"15070fc64826e1cd00665c38e2e42e5834ed359a\", \"live\": true, \"id\": \"x9c6c9733676e5240\"}, {\"sha\": \"89d992ba940c4ecaa7687d7a84ce632ead085882\", \"live\": true, \"id\": \"x8ae458b86dfe440b\"}, {\"sha\": \"54f92d19cabbd7647cd5d21d4678e8711431e9fb\", \"live\": true, \"id\": \"x8e7fd4a4b5b002c1\"}, {\"sha\": \"8d00285eae0fab5210dcfb325be7741e48c75681\", \"live\": true, \"id\": \"xeee62e178c0cfe6f\"}, {\"sha\": \"4453b349bad32a1e416e7e74af64e09e2c5810f8\", \"live\": true, \"id\": \"xdee0840c85c0add5\"}, {\"sha\": \"bb340ca13b104a9b9d1065b74a2fdfa423f817d1\", \"live\": true, \"id\": \"xa756d02df7435e1a\"}, {\"sha\": \"f65a9c4a6a8df260021fe424d562e8ff6d1741bd\", \"live\": true, \"id\": \"x6b9db70231ff254d\"}, {\"sha\": \"00808cbc066910be1220235faa7d93a9126f6a57\", \"live\": true, \"id\": \"x1855395b96b1e34f\"}, {\"sha\": \"90db973ee0fd319c596410e215a76a651601901d\", \"live\": true, \"id\": \"x477b6212a24d08da\"}, {\"sha\": \"f6cf4d0675de30afee8cab929b97000e2c41fbc8\", \"live\": true, \"id\": \"xfc8946e7c80f2800\"}, {\"sha\": \"4f522fb965ab3805f56250db187fefadb281edd2\", \"live\": true, \"id\": \"x14784d8f428fa949\"}, {\"sha\": \"849c88aa2122ea5a40bb39540acf551d3ac67728\", \"live\": true, \"id\": \"x3f2d3b6cb53f67a4\"}, {\"sha\": \"a9197ffb73ad014d477c7eb1eed41f9ea025774b\", \"live\": true, \"id\": \"xb6e923d2f396f5ab\"}, {\"sha\": \"446eef76c02fc29049012312c704c12b4b54ba8d\", \"live\": true, \"id\": \"x9ef4ca7e914c87ec\"}], \"name\": \"A\"}], \"translatedDisplayName\": \"Liczenie z ma\\u0142ymi liczbami\", \"creationDate\": \"2018-02-26T22:54:43Z\", \"editSlug\": \"edit/e/x4debd8a3\", \"sourceLanguage\": \"en\", \"deleted\": false, \"usesWorkedExamples\": false, \"translatedDescription\": \"\\u0106wiczenie w liczeniu do 10 obiekt\\u00f3w.\", \"fileName\": null, \"translatedProblemTypes\": [{\"relatedVideos\": [], \"items\": [{\"sha\": \"83a66d7bb11feb7d4c989aed6a475a009ec73dd9\", \"live\": true, \"id\": \"xde8147b8edb82294\"}, {\"sha\": \"76c7fd55c20dfe0f4f938745b73e1e7d0c4757ac\", \"live\": true, \"id\": \"xa5c8d62485b6bf16\"}, {\"sha\": \"a4318a5630a8766f55dc6830527dbaffc7fb917b\", \"live\": true, \"id\": \"x2313c50d4dfd4a0a\"}, {\"sha\": \"b350b21cf286a64c0e7869a6429fc6c5fd139c1f\", \"live\": true, \"id\": \"xcd7ba1c0c381fadb\"}, {\"sha\": \"e8ba59f1f99433fbd0360332f999cde76ab8976b\", \"live\": true, \"id\": \"x4cb56360820eece5\"}, {\"sha\": \"954fcd5885c36b841a22bc5bf89207bced27b8be\", \"live\": true, \"id\": \"xcc39b61282c884be\"}, {\"sha\": \"15070fc64826e1cd00665c38e2e42e5834ed359a\", \"live\": true, \"id\": \"x9c6c9733676e5240\"}, {\"sha\": \"89d992ba940c4ecaa7687d7a84ce632ead085882\", \"live\": true, \"id\": \"x8ae458b86dfe440b\"}, {\"sha\": \"54f92d19cabbd7647cd5d21d4678e8711431e9fb\", \"live\": true, \"id\": \"x8e7fd4a4b5b002c1\"}, {\"sha\": \"8d00285eae0fab5210dcfb325be7741e48c75681\", \"live\": true, \"id\": \"xeee62e178c0cfe6f\"}, {\"sha\": \"4453b349bad32a1e416e7e74af64e09e2c5810f8\", \"live\": true, \"id\": \"xdee0840c85c0add5\"}, {\"sha\": \"bb340ca13b104a9b9d1065b74a2fdfa423f817d1\", \"live\": true, \"id\": \"xa756d02df7435e1a\"}, {\"sha\": \"f65a9c4a6a8df260021fe424d562e8ff6d1741bd\", \"live\": true, \"id\": \"x6b9db70231ff254d\"}, {\"sha\": \"00808cbc066910be1220235faa7d93a9126f6a57\", \"live\": true, \"id\": \"x1855395b96b1e34f\"}, {\"sha\": \"90db973ee0fd319c596410e215a76a651601901d\", \"live\": true, \"id\": \"x477b6212a24d08da\"}, {\"sha\": \"f6cf4d0675de30afee8cab929b97000e2c41fbc8\", \"live\": true, \"id\": \"xfc8946e7c80f2800\"}, {\"sha\": \"4f522fb965ab3805f56250db187fefadb281edd2\", \"live\": true, \"id\": \"x14784d8f428fa949\"}, {\"sha\": \"849c88aa2122ea5a40bb39540acf551d3ac67728\", \"live\": true, \"id\": \"x3f2d3b6cb53f67a4\"}, {\"sha\": \"a9197ffb73ad014d477c7eb1eed41f9ea025774b\", \"live\": true, \"id\": \"xb6e923d2f396f5ab\"}, {\"sha\": \"446eef76c02fc29049012312c704c12b4b54ba8d\", \"live\": true, \"id\": \"x9ef4ca7e914c87ec\"}], \"name\": \"A\"}], \"authorList\": [], \"sponsored\": false, \"isQuiz\": false, \"shortDisplayName\": \"Count with \", \"curatedRelatedVideos\": [\"x9b4a5e7a\"], \"nodeSlug\": \"e/counting-out-1-20-objects\", \"tags\": [], \"hPosition\": -40, \"authorNames\": [], \"kind\": \"Exercise\", \"sha1\": \"4958b98468177d530f2a556a5f1cd0a038c74d1a\", \"displayName\": \"Count with small numbers\", \"name\": \"counting-out-1-20-objects\", \"translatedDescriptionHtml\": \"\\u0106wiczenie w liczeniu do 10 obiekt\\u00f3w.\", \"prerequisites\": [], \"contentId\": \"x4debd8a3\", \"relatedContent\": [\"video:x9b4a5e7a\"], \"title\": \"Count with small numbers\", \"backupTimestamp\": null, \"kaUrl\": \"https://pl.khanacademy.org/exercise/counting-out-1-20-objects\", \"suggestedCompletionCriteria\": \"num_problems_7\", \"sha\": \"fe136c29440e7aa557a02674acdb12353d82ee63\", \"prettyDisplayName\": \"Count with small numbers\", \"imageUrl\": \"https://cdn.kastatic.org/ka-exercise-screenshots/counting-out-1-20-objects.png\", \"translatedTitle\": \"Liczenie z ma\\u0142ymi liczbami\"}, \"proficientDate\": null, \"practiced\": false, \"contentKind\": \"UserExercise\", \"exerciseProgress\": {\"practiced\": false, \"mastered\": false, \"level\": \"unstarted\"}, \"promotions\": {}, \"kind\": \"UserExercise\", \"totalCorrect\": 1, \"masteryPoints\": 0, \"clearStrugglingIndicators\": false, \"backupTimestamp\": \"2020-03-08T08:26:19Z\", \"mastered\": false, \"fpmMasteryLevel\": \"unfamiliar\", \"kaid\": \"kaid_1111324816458145736916590\", \"actionResults\": {\"badgeCounts\": {\"0\": 0, \"1\": 0, \"2\": 0, \"3\": 0, \"4\": 0, \"5\": 0}, \"notificationsAdded\": {\"toast\": [], \"readable\": [], \"urgent\": [], \"avatarParts\": [], \"continueUrl\": \"https://pl.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-out-1-20-objects\", \"badges\": []}, \"attemptCorrect\": false, \"userProfile\": {\"countVideosCompleted\": 0, \"countBrandNewNotifications\": 0, \"streakLastLength\": 1, \"streakLength\": 1, \"points\": 150, \"streakLastExtended\": \"2020-03-10\"}, \"pointsEarned\": {\"points\": 0}, \"tutorialNodeProgress\": [{\"progress\": \"started\", \"id\": \"e/counting-out-1-20-objects\"}]}}"
    const val HY_ATTEMPT_JSON_LINK = "/com/ustadmobile/lib/contentscrapers/khan/hy/attempt.json"

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

    const val MIMETPYE_MPEG = "audio/mpeg"
    const val MIMETYPE_MKV = "video/x-matroska"
    const val MIMETYPE_ZIP = "application/zip"
    const val MIMETYPE_EPUB = "application/epub+zip"
    const val MIMETYPE_JSON = "application/json"
    const val MIMETYPE_SVG = "image/svg+xml"
    const val MIMETYPE_JPG = "image/jpg"
    const val MIMETYPE_WEBP = "image/webp"
    const val MIMETYPE_CSS = "text/css"
    const val MIMETYPE_TEXT = "text/plain"
    const val MIMETYPE_JS = "application/javascript"
    const val MIMETYPE_MP4 = "video/mp4"
    const val MIMETYPE_WEB_CHUNK = "application/webchunk+zip"
    const val MIMETYPE_HAR = "application/har+zip"
    const val MIMETYPE_TINCAN = "application/tincan+zip"
    const val MIMETYPE_WEBM = "video/webm"
    const val MIMETYPE_KHAN = "application/khan-video+zip"
    const val MIMETYPE_PDF = "application/pdf"
    const val MIMETYPE_OGG = "audio/ogg"
    const val MIMETYPE_WOFF2 = "font/woff2"
    const val MIMETYPE_OTF = "font/opentype"
    const val MIMETYPE_M4V = "video/x-m4v"


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
    const val SRT_EXT = ".srt"

    const val CK12_VIDEO = "video"
    const val CK12_PLIX = "plix"
    const val CK12_PRACTICE = "practice"
    const val CK12_READ = "read"
    const val CK12_ACTIVITIES = "activities"
    const val CK12_STUDY_AIDS = "study aids"
    const val CK12_LESSONS = "lesson plans"
    const val CK12_READ_WORLD = "real world"

    val CONTENT_MAP_CK12 = mapOf(
            CK12_VIDEO to ContentEntry.TYPE_VIDEO,
            CK12_PLIX to ContentEntry.TYPE_INTERACTIVE_EXERCISE,
            CK12_PRACTICE to ContentEntry.TYPE_INTERACTIVE_EXERCISE,
            CK12_READ to ContentEntry.TYPE_ARTICLE,
            CK12_ACTIVITIES to ContentEntry.TYPE_ARTICLE,
            CK12_STUDY_AIDS to ContentEntry.TYPE_ARTICLE,
            CK12_LESSONS to ContentEntry.TYPE_ARTICLE,
            CK12_READ_WORLD to ContentEntry.TYPE_ARTICLE)

    val IMAGE_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg")
    val VIDEO_EXTENSIONS = Arrays.asList("mp4")
    val AUDIO_EXTENSIONS = Arrays.asList("mp3")

    const val VIDEO_FILENAME_MP4 = "video.mp4"
    const val VIDEO_FILENAME_WEBM = "video.webm"
    const val TINCAN_FILENAME = "tincan.xml"
    const val SUBTITLE_FILENAME = "subtitle"

    const val ARABIC_LANG_CODE = "ar"
    const val ENGLISH_LANG_CODE = "en"
    const val USTAD_MOBILE = "Ustad Mobile"
    const val ROOT = "root"
    const val EMPTY_SPACE = " "
    const val FORWARD_SLASH = "/"
    const val KHAN = "Khan Academy"
    const val GDL = "Global Digital Library"
    const val CK12 = "CK12"
    const val HAB = "Habaybna"
    const val KHAN_PREFIX = "khan-id://"

    // khan stuff
    const val TABLE_OF_CONTENTS_ROW = "TableOfContentsRow"
    const val SUBJECT_PAGE_TOPIC_CARD = "SubjectPageTopicCard"
    const val SUBJECT_CHALLENGE = "SubjectChallenge"
    const val SUBJECT_PROGRESS = "SubjectProgress"
    const val CONTENT_LIST= "ContentList"
    const val CONTENT_DETAIL_SOURCE_URL_KHAN_ID = "content-detail?sourceUrl=khan-id://"

    const val TIME_OUT_SELENIUM_SECS = 120

    val TIME_OUT_SELENIUM: Duration by lazy {
        Duration.ofSeconds(TIME_OUT_SELENIUM_SECS.toLong())
    }

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
