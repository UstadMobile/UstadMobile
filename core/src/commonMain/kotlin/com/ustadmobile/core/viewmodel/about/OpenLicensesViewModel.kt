package com.ustadmobile.core.viewmodel.about

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase

class OpenLicensesViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle, EpubContentViewModel.DEST_NAME){

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.licenses),
                hideBottomNavigation = true,
                navigationVisible = false,
                userAccountIconVisible = false,
            )
        }
    }

    fun onClickLink(link: String) {
        onClickLinkUseCase.invoke(link, OpenExternalLinkUseCase.Companion.LinkTarget.BLANK)
    }

    companion object {

        const val DEST_NAME = "OpenLicenses"

    }
}