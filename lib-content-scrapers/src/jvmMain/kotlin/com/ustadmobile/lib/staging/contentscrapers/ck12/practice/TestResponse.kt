package com.ustadmobile.lib.contentscrapers.ck12.practice

import com.google.gson.annotations.SerializedName

class TestResponse {

    var response: Response? = null

    inner class Response {

        var test: Test? = null
        var testScore: TestScore? = null

        inner class Test {

            @SerializedName("_id")
            var id: String? = null

            var updated: String? = null

        }

        inner class TestScore {

            @SerializedName("_id")
            var id: String? = null

        }

    }

}
