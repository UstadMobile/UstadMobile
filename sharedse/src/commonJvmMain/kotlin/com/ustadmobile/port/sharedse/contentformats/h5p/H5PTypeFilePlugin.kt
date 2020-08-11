package com.ustadmobile.port.sharedse.contentformats.h5p

import com.google.gson.Gson
import com.ustadmobile.core.catalog.contenttype.H5PTypePlugin
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.H5P
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.Reader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class H5PJson(var title: String? = null)

class H5PTypeFilePlugin : H5PTypePlugin(), ContentTypeFilePlugin {

    override fun getContentEntry(file: File): ContentEntryWithLanguage? {
        var contentEntry: ContentEntryWithLanguage? = null
        try {
            ZipInputStream(FileInputStream(file)).use {
                var zipEntry: ZipEntry? = null
                while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                    val fileName = zipEntry?.name
                    if (fileName?.toLowerCase() == "h5p.json") {

                        val data = String(it.readBytes())

                        val json = Gson().fromJson(data, H5PJson::class.java)

                        contentEntry = ContentEntryWithLanguage().apply {
                            contentFlags = ContentEntry.FLAG_IMPORTED
                            licenseType = ContentEntry.LICENSE_TYPE_OTHER
                            title = json.title
                            contentFlags = ContentEntry.FLAG_IMPORTED
                            author = ""
                            description = ""
                            leaf = true
                            entryId = file.name// TODO foldername
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

    override fun importMode(): Int {
        return H5P
    }
}