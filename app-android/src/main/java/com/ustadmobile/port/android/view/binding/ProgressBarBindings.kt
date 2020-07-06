package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_MIRROR

@BindingAdapter("repoLoadStatus")
fun ProgressBar.repoLoadStatus(repoLoadStatus: RepositoryLoadHelper.RepoLoadStatus?) {
    val loadStatusFlag = repoLoadStatus?.loadStatus ?: 0
    visibility = if(loadStatusFlag == STATUS_LOADING_CLOUD || loadStatusFlag == STATUS_LOADING_MIRROR) {
        View.VISIBLE
    }else {
        View.GONE
    }
}