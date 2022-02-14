package com.ustadmobile.core.view

interface RedirectView : UstadView {

    companion object {
        val VIEW_NAME = "RedirectView"

        /**
         * Tag for web redirection: when redirected, route will be invoked but
         * app state and props wont be updated which cause to have a blank page.
         * This flag will help in updating state and props
         */
        val TAG_REDIRECTED = "app.redirected"
    }
}
