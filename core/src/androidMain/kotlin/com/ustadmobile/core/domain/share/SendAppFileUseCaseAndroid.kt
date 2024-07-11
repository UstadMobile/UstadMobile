package com.ustadmobile.core.domain.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class SendAppFileUseCaseAndroid(
    private val appContext: Context
) : SendAppFileUseCase {
    override suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            val apkFile = File(appContext.applicationInfo.sourceDir)
            val apkUri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.provider",
                apkFile
            )

            // Split name check
            val splitNames = appContext.packageManager.getPackageInfo(appContext.packageName, 0).splitNames
            if (splitNames != null && splitNames.isNotEmpty()) {
                throw IllegalArgumentException("APK has splits and cannot be shared as a single file.")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(intent, "Share App")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(chooserIntent)
        }
    }
}
