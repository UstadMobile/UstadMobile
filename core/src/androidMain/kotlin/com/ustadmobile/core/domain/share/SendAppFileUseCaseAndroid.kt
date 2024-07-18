package com.ustadmobile.core.domain.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.ustadmobile.core.MR
import com.ustadmobile.core.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class SendAppFileUseCaseAndroid(
    private val activityContext: Context
) : SendAppFileUseCase {
    override suspend operator fun invoke(shareLink: Boolean) {
        withContext(Dispatchers.Main) {
            if (shareLink) {
                shareAppLink()
            } else {
                shareApkFile()
            }
        }
    }

    private fun shareApkFile() {
        val apkFile = File(activityContext.applicationInfo.sourceDir)
        val apkUri = FileProvider.getUriForFile(
            activityContext,
            "${activityContext.packageName}.provider",
            apkFile
        )

        // Split name check
        val splitNames = activityContext.packageManager.getPackageInfo(activityContext.packageName, 0).splitNames
        if (splitNames != null && splitNames.isNotEmpty()) {
            throw IllegalArgumentException("APK has splits and cannot be shared as a single file.")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(intent, activityContext.getString(R.string.share_app_title))
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activityContext.startActivity(chooserIntent)
    }

    private fun shareAppLink() {
        val appPackageName = activityContext.packageName
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=$appPackageName"
            )
        }

        val chooserIntent = Intent.createChooser(intent, activityContext.getString(R.string.share_app_title))
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activityContext.startActivity(chooserIntent)
    }
}
