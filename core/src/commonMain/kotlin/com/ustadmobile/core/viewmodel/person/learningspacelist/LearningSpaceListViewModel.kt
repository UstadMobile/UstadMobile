package com.ustadmobile.core.viewmodel.person.learningspacelist

import app.cash.paging.PagingSource
import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.appconfigdb.SystemDbDataLayer
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndPersonDetails
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

data class LearningSpaceListUiState(
    val siteLink: String = "",
    val learningSpaceList: ListPagingSourceFactory<LearningSpaceInfo> = {
        EmptyPagingSource()
    },
)

class LearningSpaceListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(LearningSpaceListUiState())

    val uiState: Flow<LearningSpaceListUiState> = _uiState.asStateFlow()

    private val impl: UstadMobileSystemImpl by instance()

    val repo: SystemDb? = di.direct.instance<SystemDbDataLayer>().repository

    private val learningSpaceListPagingSource: ListPagingSourceFactory<LearningSpaceInfo> = {
        repo?.learningSpaceInfoDao()?.findAllAsPagingSource()?:EmptyPagingSource()
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MR.strings.learning_space),
                userAccountIconVisible = false,
                navigationVisible = false,
            )
        }

        _uiState.update { prev ->
            prev.copy(
                siteLink = savedStateHandle[KEY_LINK] ?: "",
                learningSpaceList = learningSpaceListPagingSource
            )
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
