package com.ustadmobile.port.sharedse.contentformats.h5p

import com.ustadmobile.core.catalog.contenttype.H5PContentType
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.util.UMUtil
import com.ustadmobile.port.sharedse.contentformats.ContentTypePlugin
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
                        val h5pJsonString = UMIOUtils.readStreamToString(zipIn)
                        val h5pJsonObj = Json.parse<H5PContentSerializer>(h5pJsonString)

                        contentEntry = ContentEntry()
                        contentEntry!!.imported = true
                        contentEntry!!.title = h5pJsonObj.title
                        val description = h5pJsonObj.metaDescription ?: ""
                        val license = h5pJsonObj.license ?: ""
                        val author = h5pJsonObj.author ?: ""
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
}
