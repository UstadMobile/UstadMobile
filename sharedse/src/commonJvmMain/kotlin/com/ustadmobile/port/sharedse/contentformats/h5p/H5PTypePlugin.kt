package com.ustadmobile.port.sharedse.contentformats.h5p

import com.ustadmobile.core.catalog.contenttype.H5PContentType
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.util.UMUtil
import com.ustadmobile.port.sharedse.contentformats.ContentTypePlugin
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Class which handles H5P content import tasks, it checks validity of the h5P contents
 *
 * @author kileha3
 */
class H5PTypePlugin : H5PContentType(), ContentTypePlugin {

    //This declaration is experimental and its usage must be marked with '' or '@UseExperimental(kotlinx.serialization.ImplicitReflectionSerializer::class)'
    @kotlinx.serialization.ImplicitReflectionSerializer
    override fun getContentEntry(file: File): ContentEntry? {
        var contentEntry: ContentEntry? = null
        try {
            ZipInputStream(FileInputStream(file)).use { zipIn ->
                var zipEntry: ZipEntry? = null
                while ({ zipEntry = zipIn.nextEntry; zipEntry }() != null) {

                    val fileName = zipEntry!!.name
                    if (fileName == "h5p.json") {
                        val jsonStr = UMIOUtils.readStreamToString(zipIn)
                        val jsonObj = Json.parse<HashMap<String,String>>(jsonStr)

                        contentEntry = ContentEntry()
                        contentEntry!!.imported = true
                        contentEntry!!.title = jsonObj[TITLE_TAG]
                        val description = if (jsonObj.containsKey(DESCRIPTION_TAG))
                            jsonObj[DESCRIPTION_TAG]
                        else
                            ""
                        val license = if (jsonObj.containsKey(LICENSE_TAG)) jsonObj[LICENSE_TAG] else ""
                        val author = if (jsonObj.containsKey(AUTHOR_TAG)) jsonObj[AUTHOR_TAG] else ""
                        contentEntry!!.author = author
                        contentEntry!!.description = description
                        contentEntry!!.licenseType = UMUtil.getH5pLicenceId(license)
                        contentEntry!!.leaf = true
                        break
                    }

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return contentEntry
    }

    companion object {

        private const val TITLE_TAG = "title"

        private const val DESCRIPTION_TAG = "metaDescription"

        private const val AUTHOR_TAG = "author"

        private const val LICENSE_TAG = "license"
    }
}
