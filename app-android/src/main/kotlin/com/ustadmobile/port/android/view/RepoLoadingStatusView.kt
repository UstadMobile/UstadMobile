package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.toughra.ustadmobile.R

class RepoLoadingStatusView: CoordinatorLayout{

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init(){
        View.inflate(context, R.layout.view_repo_loading_status, this)
    }
}