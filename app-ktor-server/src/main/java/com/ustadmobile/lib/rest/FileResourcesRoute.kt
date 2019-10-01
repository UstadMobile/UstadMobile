package com.ustadmobile.lib.rest

import com.ustadmobile.core.util.UMFileUtil
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import java.io.File
import java.io.FileInputStream
import com.ustadmobile.port.sharedse.util.UmFileUtilSe

fun Route.FileResourcesRoute() {

    get("H5PResources/{paths...}"){
        var resourcePath = call.parameters.getAll("paths")?.joinToString("/") ?: ""
        resourcePath = if(resourcePath.contains("core/")) "/"+resourcePath.replace("core","core/contentformats")
        else "/com/ustadmobile/core/contentformats/h5p/$resourcePath"
        println(resourcePath)
        val tempFile = File.createTempFile("testFile", "tempFile")
        UmFileUtilSe.extractResourceToFile(resourcePath, tempFile)
        call.respondBytes(FileInputStream(tempFile).readBytes(),
                UMFileUtil.getContentType(resourcePath), HttpStatusCode.OK)
    }
}