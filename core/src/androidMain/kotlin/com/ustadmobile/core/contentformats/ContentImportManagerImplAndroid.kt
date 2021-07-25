package com.ustadmobile.core.contentformats

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.networkmanager.DownloadNotificationService
import com.ustadmobile.lib.db.entities.ContainerImportJob
import org.kodein.di.DI

class ContentImportManagerImplAndroid(contentPlugins: List<ContentTypePlugin>, context: Any, endpoint: Endpoint, di: DI) : ContentImportManagerImpl(contentPlugins, context, endpoint, di) {

    override suspend fun queueImportContentFromFile(uri: String,
                                                    metadata: ImportedContentEntryMetaData,
                                                    containerBaseDir: String,
                                                    importMode: Int,
                                                    conversionParams: Map<String, String>): ContainerImportJob {
        val importJob =  super.queueImportContentFromFile(uri, metadata, containerBaseDir, importMode, conversionParams)

        val androidContext = context as Context
        val importIntent = Intent(androidContext, DownloadNotificationService::class.java)
        importIntent.action = DownloadNotificationService.ACTION_PREPARE_IMPORT
        importIntent.putExtra(DownloadNotificationService.EXTRA_IMPORTJOB_UID, importJob.cijUid)
        importIntent.putExtra(DownloadNotificationService.EXTRA_ENDPOINT, endpoint.url)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidContext.startForegroundService(importIntent)
        } else {
            androidContext.startService(importIntent)
        }

        return importJob
    }
}