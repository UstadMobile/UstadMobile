package com.ustadmobile.core.contentformats.opds

import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_ALIGNMENT
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_AUTHOR
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_CREATED
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_ID
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_LICENSE
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_LINK
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_LRMI
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_PUBLISHER
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_SUMMARY
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_TARGET
import com.ustadmobile.core.contentformats.opds.OpdsEntry.Companion.ATTR_TITLE
import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants

class OpdsFeed {

    lateinit var id: String
    lateinit var title: String

    var author: String? = null
    var category: String? = null
    var summary: String? = null

    var linkList: MutableList<OpdsLink> = mutableListOf()

    var entryList: MutableList<OpdsEntry> = mutableListOf()


    fun loadFromParser(xpp: XmlPullParser) {

        var evtType = xpp.getEventType()

        do {

            if (evtType == XmlPullParserConstants.START_TAG) {
                var name = xpp.getName()
                when (name) {
                    TAG_ID -> id = xpp.nextText()!!
                    TAG_TITLE -> title = xpp.nextText()!!
                    TAG_ENTRY -> {
                        val entry = OpdsEntry()
                        do {
                            if (evtType == XmlPullParserConstants.START_TAG) {
                                name = xpp.getName()
                                when (name) {
                                    ATTR_ID -> entry.id = xpp.nextText()!!
                                    ATTR_TITLE -> entry.title = xpp.nextText()!!
                                    ATTR_LINK -> entry.linkList.add(createLink(xpp))
                                    ATTR_PUBLISHER -> entry.publisher = xpp.nextText()
                                    ATTR_SUMMARY -> entry.summary = xpp.nextText()
                                    ATTR_AUTHOR -> {
                                        xpp.nextTag()
                                        entry.author = xpp.nextText()
                                    }
                                    ATTR_LRMI -> {
                                        entry.targetName = xpp.getAttributeValue(null, ATTR_TARGET)
                                        entry.readingLevel = xpp.getAttributeValue(null, ATTR_ALIGNMENT)
                                    }
                                    ATTR_LICENSE -> {
                                        entry.license = xpp.nextText()
                                    }
                                }
                            }
                            evtType = xpp.next()

                        } while (!(evtType == XmlPullParserConstants.END_TAG && xpp.getName() == "entry"))

                        entryList.add(entry)

                    }
                    TAG_LINK -> {
                        linkList.add(createLink(xpp))

                    }
                }


            }

            evtType = xpp.next()

        } while (evtType != XmlPullParserConstants.END_DOCUMENT)


    }

    private fun createLink(xpp: XmlPullParser): OpdsLink {
        val link = OpdsLink()
        link.href = xpp.getAttributeValue(null, OpdsLink.ATTR_HREF)!!
        link.rel = xpp.getAttributeValue(null, OpdsLink.ATTR_REL)!!
        link.title = xpp.getAttributeValue(null, OpdsLink.ATTR_TITLE)
        link.type = xpp.getAttributeValue(null, OpdsLink.ATTR_TYPE)
        link.activeFacet = xpp.getAttributeValue(null, OpdsLink.ATTR_FACET_ACTIVE)?.toBoolean()
        link.facetGroup = xpp.getAttributeValue(null, OpdsLink.ATTR_FACET_GROUP)
        return link
    }

    companion object {

        const val TAG_ID = "id"

        const val TAG_TITLE = "title"

        const val TAG_UPDATED = "updated"

        const val TAG_LINK = "link"

        const val TAG_ENTRY = "entry"

    }

}