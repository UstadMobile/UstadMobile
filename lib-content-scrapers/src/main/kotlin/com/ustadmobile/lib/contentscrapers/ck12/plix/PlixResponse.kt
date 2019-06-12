package com.ustadmobile.lib.contentscrapers.ck12.plix

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlixResponse {

    @Expose
    @SerializedName("response")
    var response: Response? = null

    class Response {
        @Expose
        @SerializedName("question")
        var question: Question? = null
    }

    class Question {
        @Expose
        @SerializedName("updated")
        var updated: String? = null
        @Expose
        @SerializedName("created")
        var created: String? = null
        @Expose
        @SerializedName("_id")
        var _id: String? = null
    }
}
