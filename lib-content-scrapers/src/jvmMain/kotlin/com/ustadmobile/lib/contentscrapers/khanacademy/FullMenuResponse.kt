package com.ustadmobile.lib.contentscrapers.khanacademy

class FullMenuResponse {

    val data: Data? = null

    inner class Data {

        val learnMenuTopics: List<Topic>? = null

        inner class Topic {

            val slug: String? = null

            val translatedTitle: String? = null

            val href: String? = null

            val children: List<Topic>? = null

        }

    }

}