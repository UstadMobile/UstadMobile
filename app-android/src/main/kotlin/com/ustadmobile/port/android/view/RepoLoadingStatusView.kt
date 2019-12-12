package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.ustadmobile.door.RepositoryLoadHelper

class RepoLoadingStatusView: View, RepositoryLoadHelper.RepoLoadCallback {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init(){
        //TODO: load from layout xml
    }

    override fun onLoadStatusChanged(status: Int, remoteDevice: String?) {

    }
}