package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PDFContentView
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

actual class PDFContentPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                    view: PDFContentView, di: DI)
    : PDFContentPresenterCommon(context, arguments, view, di) {

    var container: Container? = null

    private val systemImpl: UstadMobileSystemImpl by instance()

    actual override fun handleOnResume() {
        GlobalScope.launch {

            if (view.filePath == null) {

                val containerResult = db.containerDao.findByUidAsync(containerUid)
                if (containerResult == null) {
                    view.showSnackBar(systemImpl.getString(MessageID.no_pdf_file_found, context), {}, 0)
                    view.loading = false
                    return@launch
                }
                container = containerResult
                val result = db.containerEntryDao.findByContainerAsync(containerUid)
                for (entry in result) {

                    val containerEntryPath = entry.cePath
                    val containerEntryFile = entry.containerEntryFile

                    if (containerEntryPath != null && containerEntryFile != null) {
                        if (PDF_EXT_LIST.any { containerEntryPath.toLowerCase().endsWith(it) }) {
                            pdfPath = containerEntryFile.cefPath
                        }
                    }
                }


            }

            view.runOnUiThread(kotlinx.coroutines.Runnable {
                view.filePath = pdfPath
            })

        }

    }

}