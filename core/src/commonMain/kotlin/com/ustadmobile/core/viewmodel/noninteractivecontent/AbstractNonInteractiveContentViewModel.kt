package com.ustadmobile.core.viewmodel.noninteractivecontent

import com.ustadmobile.core.domain.xapi.model.XapiActivity
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder.NonInteractiveContentXapiStatementRecorderFactory
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadViewModel
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Common base ViewModel that handles non-interactive content (e.g. video, pdf, epub). Although it
 * would be nice to do such things via composition instead of inheritence, generating statements
 * uses various protected variables from UstadViewModel (e.g. savedStateHandle, account manager, etc).
 */
abstract class AbstractNonInteractiveContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String,
) : UstadViewModel(di, savedStateHandle, destName) {

    data class TitleAndLangCode(
        val title: String,
        val langCode: String,
    )

    private val statementRecorderFactory: NonInteractiveContentXapiStatementRecorderFactory =
        di.onActiveEndpoint().direct.instance()

    protected val contentEntryUid = savedStateHandle[ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0

    /**
     * Abstract val to be implemented by the underlying viewmodel. Used when generating statements
     */
    abstract val titleAndLangCode: TitleAndLangCode?

    protected val xapiSession = createXapiSession(contentEntryUid = contentEntryUid)

    private val statementRecorder by lazy {
        statementRecorderFactory.newStatementRecorder(
            xapiSession = createXapiSession(
                contentEntryUid = contentEntryUid
            ),
            scope = viewModelScope,
            xapiActivityProvider = {
                val titleAndLangCodeVal = titleAndLangCode

                XapiActivityStatementObject(
                    id = xapiSession.rootActivityId!!, //This is set in init
                    definition = XapiActivity(
                        name = if(titleAndLangCodeVal != null) {
                            mapOf(titleAndLangCodeVal.langCode to titleAndLangCodeVal.title)
                        }else {
                            null
                        }
                    )
                )
            }
        )
    }


    /**
     * To be invoked by the ViewModel when the content being displayed is complete e.g. reached
     * last page of book, video finished, etc.
     */
    fun onComplete() {
        statementRecorder.onComplete()
    }


    /**
     * To be invoked by the ViewModel when the user has progressed through the content e.g. video
     * is advancing, scrolling through a book/document, etc.
     */
    fun onProgressed(progress: Int) {
        statementRecorder.onProgressed(progress)
    }


    /**
     * Invoked by the ViewModel. In the case of video, generally linked to the play/pause state. In
     * the case of books/documents, normally determined by whether or not the window is visible/active.
     */
    fun onActiveChanged(active: Boolean) {
        statementRecorder.onActiveChanged(active)
    }

    /**
     * Invoked by the ViewModel onCleared
     */
    override fun onCleared() {
        statementRecorder.onCleared()

        super.onCleared()
    }

    /**
     * Used on the web.
     */
    fun onUnload() {
        statementRecorder.onUnload()
    }



}