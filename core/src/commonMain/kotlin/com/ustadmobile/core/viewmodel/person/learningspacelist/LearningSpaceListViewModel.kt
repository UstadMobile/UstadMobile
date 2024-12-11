package com.ustadmobile.core.viewmodel.person.learningspacelist

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.learningspace.GoToLearningSpaceUseCase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel
import com.ustadmobile.systemdb.model.LearningSpaceInfo
import com.ustadmobile.systemdb.repo.LearningSpaceRepository
import com.ustadmobile.systemdb.repo.SystemDbRepository
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

data class LearningSpaceListUiState(
    val siteLink: String = "",
    val learningSpaces: List<LearningSpaceInfo> = emptyList(),
)


class LearningSpaceListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
) : UstadListViewModel<LearningSpaceListUiState>(
    di, savedStateHandle, LearningSpaceListUiState(), DEST_NAME,
) {

    private val impl: UstadMobileSystemImpl by instance()

    private val goToLearningSpaceUseCase:GoToLearningSpaceUseCase by instance()

    val repo: LearningSpaceRepository = di.direct.instance<SystemDbRepository>().learningSpaceRepository

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
        val args = buildMap {
            putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            put(ARG_LEARNINGSPACE_URL, learningSpace)
        }

      goToLearningSpaceUseCase.invoke(
          learningSpace,
          navController,
          args,
          viewName
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
