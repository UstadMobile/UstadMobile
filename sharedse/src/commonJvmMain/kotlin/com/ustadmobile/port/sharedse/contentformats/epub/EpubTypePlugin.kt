package com.ustadmobile.port.sharedse.contentformats.epub

import com.ustadmobile.core.catalog.contenttype.EPUBType
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.ContentTypePlugin

import org.xmlpull.v1.XmlPullParserException

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_OTHER

/**
 * Class which handles EPUB content import tasks, creates content entry from the H5P file
 */
class EpubTypePlugin : EPUBType(), ContentTypePlugin {

    override fun getContentEntry(file: File): ContentEntry? {
        var contentEntry: ContentEntry? = null
        try {
            ZipInputStream(FileInputStream(file)).use {
                var zipEntry: ZipEntry? = null
                while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                    val fileName = zipEntry?.name
                    if (fileName!!.contains(".opf")) {
                        val xpp = UstadMobileSystemImpl.instance.newPullParser(it)
                        val opfDocument = OpfDocument()
                        opfDocument.loadFromOPF(xpp)
                        val contentEntryVal = ContentEntry()
                        contentEntryVal.contentFlags = ContentEntry.FLAG_IMPORTED
                        contentEntryVal.licenseType = LICENSE_TYPE_OTHER
                        contentEntryVal.title = opfDocument.title
                        contentEntryVal.author = opfDocument.getCreator(0).creator
                        contentEntryVal.description = opfDocument.description
                        contentEntryVal.leaf = true
                        contentEntry = contentEntryVal
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
