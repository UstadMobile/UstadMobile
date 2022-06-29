package com.ustadmobile.core.controller

import com.ustadmobile.core.view.PDFContentView
import org.kodein.di.DI

expect class PDFContentPresenter(context: Any, arguments: Map<String, String>, view: PDFContentView,
                                 di: DI)
    : PDFContentPresenterCommon {

    override fun handleOnResume()
}