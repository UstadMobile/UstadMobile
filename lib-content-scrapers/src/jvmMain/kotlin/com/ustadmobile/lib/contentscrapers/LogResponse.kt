package com.ustadmobile.lib.contentscrapers

class LogResponse {

    var message: Message? = null

    inner class Message {

        var method: String? = null

        var params: Params? = null

        inner class Params {

            var response: Response? = null

            var redirectResponse: Response? = null

            inner class Response {

                var mimeType: String? = null

                var url: String? = null

                var headers: Map<String, String>? = null

                var requestHeaders: Map<String, String>? = null

            }

        }

    }

}
