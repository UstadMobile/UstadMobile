package com.ustadmobile.port.sharedse.view

import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.view.UstadView

interface DownloadDialogView : UstadView {

    fun setBottomButtonsVisible(visible: Boolean)

    fun setBottomButtonPositiveText(text: String)

    fun setBottomButtonNegativeText(text: String)

    fun setDownloadOverWifiOnly(wifiOnly: Boolean)

    fun setStatusText(statusText: String, totalItems: Int, sizeInfo: String)

    fun setStackedOptions(optionIds: IntArray, optionTexts: Array<String>)

    fun setStackOptionsVisible(visible: Boolean)

    fun dismissDialog()

    fun setWifiOnlyOptionVisible(visible: Boolean)

    fun setCalculatingViewVisible(visible: Boolean)

    fun showStorageOptions(storageOptions: List<UMStorageDir>)

    fun cancelOrPauseDownload(jobId: Long, cancel: Boolean)

    companion object {

        const val VIEW_NAME = "DownloadDialog"
    }
}
