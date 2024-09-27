package com.ustadmobile.core.viewmodel.person.learningspacelist

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance

data class LearningSpaceListUiState(
    val siteLink: String = "",

)

class LearningSpaceListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(LearningSpaceListUiState())

    val uiState: Flow<LearningSpaceListUiState> = _uiState.asStateFlow()


    private val impl: UstadMobileSystemImpl by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MR.strings.learning_space),
                userAccountIconVisible = false,
                navigationVisible = false,
            )
        }

        _uiState.update { prev ->
            prev.copy(siteLink = savedStateHandle[KEY_LINK] ?: "")
        }
    }

    fun onClickNext() {
        navController.navigate(
            LearningSpaceEnterLinkViewModel.DEST_NAME,
            args = buildMap {
                putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            }
        )



    }


    companion object {

        const val DEST_NAME = "LearningSpaceList"

        val ARGS_TO_PASS_THROUGH = listOf(
            ARG_NEXT, UstadView.ARG_INTENT_MESSAGE, ARG_DONT_SET_CURRENT_SESSION,
        )

        val KEY_LINK = "stateUrl"

    }

}
