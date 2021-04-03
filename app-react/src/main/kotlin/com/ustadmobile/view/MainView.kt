package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView

interface MainComponentView: UstadView {
    fun updateDrawerState()
    fun onThemeChange()
}