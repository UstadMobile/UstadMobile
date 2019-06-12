package com.ustadmobile.lib.contentscrapers.khanacademy

import java.util.*

class ItemData {

    var question: Content? = null

    var hints: ArrayList<Content>? = null

    inner class Content {

        var content: String? = null

        var images: MutableMap<String, Image>? = null

        var widgets: Map<String, Widget>? = null

        inner class Image {

            var width: Int = 0

            var height: Int = 0

        }

        inner class Widget {

            var options: Options? = null

            inner class Options {

                var options: ArrayList<Option>? = null

                inner class Option {

                    internal var content: String? = null

                }

            }

        }

    }
}
