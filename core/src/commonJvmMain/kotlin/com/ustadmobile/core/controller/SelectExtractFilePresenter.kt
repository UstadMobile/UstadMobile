package com.ustadmobile.core.controller

import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.io.ext.deleteRecursively
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.core.view.SelectExtractFileView
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

import com.ustadmobile.door.DoorUri

actual class SelectExtractFilePresenter actual constructor(
    context: Any,
    arguments: Map<String, String>,
    view: SelectExtractFileView,
    di: DI,
) : SelectExtractFilePresenterCommon(context, arguments, view, di){

    private val pluginManager: ContentPluginManager by on(accountManager.activeAccount).instance()

    override suspend fun extractMetadata(uri: String, filename: String): MetadataResult {
        val doorUri = DoorUri.parse(uri)

        val tmpDir = createTemporaryDir("content")
        try {
            return ContentJobProcessContext(
                doorUri, tmpDir,
                mutableMapOf(), null, di
            ).use { contentJobProcessContext ->
                pluginManager.extractMetadata(doorUri, contentJobProcessContext)
            }
        }finally {
            tmpDir.deleteRecursively()
        }
    }

}