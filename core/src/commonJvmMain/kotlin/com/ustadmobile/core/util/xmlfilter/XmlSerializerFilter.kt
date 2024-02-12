package com.ustadmobile.core.util.xmlfilter

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer

/**
 * Implement this interface to control some of the passXmlThrough methods .  This can be used
 * to add extra output to be serialized or to stop processing.
 */
interface XmlSerializerFilter {

    /**
     * Called before the given event is passed through to the XmlSerializer.
     *
     * @param evtType The event type from the parser
     * @param parser The XmlPullParser being used
     * @param serializer The XmlSerializer being used
     *
     * @return true to continue processing, false to end processing
     */
    fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

    /**
     * Called after the given event was passed through to the XmlSerializer.
     *
     * @param evtType The event type from the parser
     * @param parser The XmlPullParser being used
     * @param serializer The XmlSerializer being used
     *
     * @return true to continue processing, false to end processing
     */
    fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

}