package com.ustadmobile.core.view

interface RedirectView : UstadView {

    fun showNextScreen(viewName: String, args: Map<String, String>)

    companion object {
        val VIEW_NAME = "RedirectView"
    }
}
