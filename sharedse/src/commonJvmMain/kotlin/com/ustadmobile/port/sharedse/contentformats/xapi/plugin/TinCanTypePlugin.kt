package com.ustadmobile.port.sharedse.contentformats.xapi.plugin

import com.ustadmobile.core.catalog.contenttype.TinCanType
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.ContentTypePlugin
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class TinCanTypePlugin : TinCanType(), ContentTypePlugin {

    override fun getContentEntry(file: File): ContentEntry? {
        var contentEntry: ContentEntry? = null
        try {
            ZipInputStream(FileInputStream(file)).use {
                var zipEntry: ZipEntry? = null
                while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                    val fileName = zipEntry?.name
                    if (fileName?.toLowerCase() == TINCAN_XML) {
                        val xpp = UstadMobileSystemImpl.instance.newPullParser(it)
                        val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        if(activity == null)
                            throw IOException("TinCanXml from ${file.absolutePath} has no launchActivity!")

                        contentEntry = ContentEntry().apply {
                            contentFlags = ContentEntry.FLAG_IMPORTED
                            licenseType = ContentEntry.LICENSE_TYPE_OTHER
                            title = activity.name
                            author = ""
                            description = activity.desc
                            leaf = true
                        }
                        break
                    }

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }

        return contentEntry
    }
}