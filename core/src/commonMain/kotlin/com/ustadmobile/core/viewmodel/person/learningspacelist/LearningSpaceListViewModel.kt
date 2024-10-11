package com.ustadmobile.core.viewmodel.person.learningspacelist

import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.appconfigdb.SystemDbDataLayer
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

data class LearningSpaceListUiState(
    val siteLink: String = "",
    val learningSpaceList: ListPagingSourceFactory<LearningSpaceInfo> = {
        EmptyPagingSource()
    },
)


class LearningSpaceListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
) : UstadListViewModel<LearningSpaceListUiState>(
    di, savedStateHandle, LearningSpaceListUiState(), LearningSpaceListViewModel.DEST_NAME,
) {


    private val impl: UstadMobileSystemImpl by instance()

    val repo: SystemDb? = di.direct.instance<SystemDbDataLayer>().repository

    private val learningSpaceListPagingSource: ListPagingSourceFactory<LearningSpaceInfo> = {
        repo?.learningSpaceInfoDao()?.findAllAsPagingSource() ?: EmptyPagingSource()
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
    fun onSelectLearningSpace(learningSpace:String) {
        val viewName =if(savedStateHandle[SignUpViewModel.ARG_NEW_OR_EXISTING_USER]=="new"){
            RegisterAgeRedirectViewModel.DEST_NAME
        }else{
            LoginViewModel.DEST_NAME
        }
        navController.navigate(
            viewName,
            args = buildMap {
                putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
                    put(ARG_LEARNINGSPACE_URL, learningSpace)

            }

        )


    }

    companion object {

        const val DEST_NAME = "LearningSpaceList"

        val KEY_LINK = "stateUrl"

    }

    override fun onUpdateSearchResult(searchText: String) {
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        //Do nothing
    }

}
