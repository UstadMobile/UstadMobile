package com.ustadmobile.port.sharedse.contentformats.h5p

import com.ustadmobile.core.catalog.contenttype.H5PTypePlugin
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.H5P
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


val licenseMap = mapOf(
        "CC-BY" to ContentEntry.LICENSE_TYPE_CC_BY,
        "CC BY-SA" to ContentEntry.LICENSE_TYPE_CC_BY_SA,
        "CC BY-ND" to ContentEntry.LICENSE_TYPE_CC_BY,
        "CC BY-NC" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC BY-NC-SA" to ContentEntry.LICENSE_TYPE_CC_BY_NC_SA,
        "CC CC-BY-NC-CD" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC0 1.0" to ContentEntry.LICENSE_TYPE_CC_0,
        "GNU GPL" to ContentEntry.LICENSE_TYPE_OTHER,
        "PD" to ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN,
        "ODC PDDL" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC PDM" to ContentEntry.LICENSE_TYPE_OTHER,
        "C" to ContentEntry.ALL_RIGHTS_RESERVED,
        "U" to ContentEntry.LICENSE_TYPE_OTHER
)

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

                        val json = Json.parseJson(data)

                        // take the name from the role Author otherwise take last one
                        var author: String? = ""
                        var name: String? = ""
                        json.jsonObject["authors"]?.jsonArray?.forEach {
                            name = it.jsonObject["name"]?.content ?: ""
                            val role = it.jsonObject["role"]?.content ?: ""
                            if (role == "Author") {
                                author = name
                            }
                        }
                        if (author.isNullOrEmpty()) {
                            author = name
                        }

                        contentEntry = ContentEntryWithLanguage().apply {
                            contentFlags = ContentEntry.FLAG_IMPORTED
                            contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                            licenseType = licenseMap[json.jsonObject["license"] ?: ""]
                                    ?: ContentEntry.LICENSE_TYPE_OTHER
                            title = if(json.jsonObject["title"]?.content.isNullOrEmpty())
                                file.nameWithoutExtension else json.jsonObject["title"]?.content
                            this.author = author
                            leaf = true
                            entryId = file.name
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