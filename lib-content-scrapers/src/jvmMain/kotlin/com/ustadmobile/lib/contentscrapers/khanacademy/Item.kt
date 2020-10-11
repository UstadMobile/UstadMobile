package com.ustadmobile.lib.contentscrapers.khanacademy

class Item {

    var error: Error? = null

    var data: Data? = null

    class Data {

        var assessmentItem: AssessmentItem? = null

        class AssessmentItem {

            var item: ItemData? = null

            var error: Error? = null

            var __typename: String? = null

            class ItemData {

                var id: String? = null

                var sha: String? = null

                var problemType: String? = null

                var itemData: String? = null

                var __typename: String? = null


            }

        }

    }

    class Error {

        var code: String?= null

        var debugMessage: String ?= null

        var __typename: String? = null

    }

}