package com.ustadmobile.core.view

interface RedirectView : UstadView {

    var showGetStarted: Boolean?

    companion object {
        val VIEW_NAME = "RedirectView"
    }
}
