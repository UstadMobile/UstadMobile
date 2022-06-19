package com.ustadmobile.core.controller

import com.ustadmobile.core.view.SelectExtractFileView
import org.kodein.di.DI

expect class SelectExtractFilePresenter(
    context: Any,
    arguments: Map<String, String>,
    view: SelectExtractFileView,
    di: DI
): SelectExtractFilePresenterCommon {
}