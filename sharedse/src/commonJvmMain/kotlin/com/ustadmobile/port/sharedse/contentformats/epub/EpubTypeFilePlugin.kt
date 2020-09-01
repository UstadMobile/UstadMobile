package com.ustadmobile.port.sharedse.contentformats.epub

import com.ustadmobile.core.catalog.contenttype.EpubTypePlugin
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin

import org.xmlpull.v1.XmlPullParserException

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_OTHER
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.ZIPPED
import java.net.URI

/**
 * Class which handles EPUB content import tasks, creates content entry from the H5P file
 */
class EpubTypeFilePlugin : EpubTypePlugin(), ContentTypeFilePlugin {

    override fun getContentEntry(file: String): ContentEntryWithLanguage? {
        var contentEntry: ContentEntryWithLanguage? = null
        try {
            ZipInputStream(FileInputStream(File(file))).use {
                var zipEntry: ZipEntry? = null
                while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                    val fileName = zipEntry?.name
                    if (fileName!!.contains(".opf")) {
                        val xpp = UstadMobileSystemImpl.instance.newPullParser(it)
                        val opfDocument = OpfDocument()
                        opfDocument.loadFromOPF(xpp)
                        val contentEntryVal = ContentEntryWithLanguage()
                        contentEntryVal.contentFlags = ContentEntry.FLAG_IMPORTED
                        contentEntryVal.contentTypeFlag = ContentEntry.TYPE_EBOOK
                        contentEntryVal.licenseType = LICENSE_TYPE_OTHER
                        contentEntryVal.title = opfDocument.title
                        contentEntryVal.author = opfDocument.getCreator(0)?.creator
                        contentEntryVal.description = opfDocument.description
                        contentEntryVal.leaf = true
                        contentEntryVal.entryId = opfDocument.id
                        val languageCode = opfDocument.getLanguage(0)
                        if (languageCode != null) {
                            val language = Language()
                            language.iso_639_1_standard = languageCode
                            contentEntryVal.language = language
                        }
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

    override fun importMode(): Int {
        return ZIPPED
    }

}
