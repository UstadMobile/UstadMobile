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
                        contentEntry = ContentEntry()
                        contentEntry!!.status = ContentEntry.STATUS_IMPORTED
                        contentEntry!!.licenseType = LICENSE_TYPE_OTHER
                        contentEntry!!.title = opfDocument.title
                        contentEntry!!.author = opfDocument.getCreator(0).creator
                        contentEntry!!.description = opfDocument.description
                        contentEntry!!.leaf = true
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
