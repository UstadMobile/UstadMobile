package com.ustadmobile.core.contentformats.opds

class OpdsLink {

    lateinit var href: String

    lateinit var rel: String

    var title: String? = null

    var type: String? = null

    var activeFacet: Boolean? = null

    var facetGroup: String? = null

    var thrCount: Int = -1

    companion object {

        const val ATTR_REL = "rel"

        const val ATTR_HREF = "href"

        const val ATTR_TITLE = "title"

        const val ATTR_TYPE = "type"

        const val ATTR_FACET_GROUP = "opds:facetGroup"

        const val ATTR_THR_COUNT = "thr:count"

        const val ATTR_FACET_ACTIVE = "opds:activeFacet"
    }

}