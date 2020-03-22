package com.ustadmobile.lib.contentscrapers.ck12.practice

import com.google.gson.annotations.SerializedName

class PracticeResponse {

    var response: TestResponse? = null

    inner class TestResponse {

        var test: Test? = null

        inner class Test {

            @SerializedName("_id")
            var id: String? = null

            @SerializedName("goal")
            var goal: Int = 0

            @SerializedName("questionsCount")
            var questionsCount: Int = 0

            var title: String? = null

            var nextPractice: NextPractice? = null

            var updated: String? = null

            inner class NextPractice {

                @SerializedName("_id")
                var id: String? = null

                @SerializedName("handle")
                var nameOfNextPractice: String? = null

            }
        }

    }

}
