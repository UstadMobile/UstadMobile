package com.ustadmobile.core.viewmodel.xapicontent

import com.ustadmobile.core.domain.contententry.launchcontent.xapi.ResolveXapiLaunchHrefUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class XapiContentUiState(
    val url: String? = null,
    val contentEntryVersionUid: Long = 0,
)

class XapiContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val resolveXapiLaunchHrefUseCase: ResolveXapiLaunchHrefUseCase by di.onActiveEndpoint()
        .instance()

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val _uiState = MutableStateFlow(
        XapiContentUiState(contentEntryVersionUid = entityUidArg)
    )

    val uiState: Flow<XapiContentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val xapiSession = createXapiSession(entityUidArg)
                val launchHrefResult = resolveXapiLaunchHrefUseCase(entityUidArg, xapiSession)
                _uiState.update { prev ->
                    prev.copy(url = launchHrefResult.url)
                }

                _appUiState.update { prev ->
                    prev.copy(
                        title = launchHrefResult.launchActivity.name,
                        hideBottomNavigation = true
                    )
                }
            }catch(e: Throwable) {
                Napier.e("Exception opening xapi content", e)
            }
        }
    }

    companion object {

        const val DEST_NAME = "XapiContent"

    }

}