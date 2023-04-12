package com.ustadmobile.core.contentformats.opds

class OpdsEntry {

    lateinit var id: String

    lateinit var title: String

    var author: String? = null

    var publisher: String? = null

    var license: String? = null

    var summary: String? = null

    var readingLevel: String? = null

    var targetName: String? = null

    var linkList: MutableList<OpdsLink> = mutableListOf()

    companion object {

        const val ATTR_ID = "id"

        const val ATTR_AUTHOR = "author"

        const val ATTR_TITLE = "title"

        const val ATTR_PUBLISHER = "dc:publisher"

        const val ATTR_LICENSE = "dc:license"

        const val ATTR_UPDATED = "updated"

        const val ATTR_SUMMARY = "summary"

        const val ATTR_LINK = "link"

        const val ATTR_CREATED = "created"

        const val ATTR_LRMI = "lrmi:educationalAlignment"

        const val ATTR_ALIGNMENT = "alignmentType"

        const val ATTR_TARGET = "targetName"

    }

}