package com.ustadmobile.core.viewmodel.courseblock.textblockdetail

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class TextBlockDetailUiState(
    val courseBlock: CourseBlock? = null,
)

class TextBlockDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<CourseBlock>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(TextBlockDetailUiState())

    val uiState: Flow<TextBlockDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.courseBlockDao.findByUidAsyncAsFlow(entityUidArg).collect {
                        _uiState.update { prev ->
                            prev.copy(courseBlock = it)
                        }
                        _appUiState.update { prev ->
                            prev.copy(title = it?.cbTitle)
                        }
                    }
                }
            }
        }

    }

    companion object {

        const val DEST_NAME = "CourseText"

    }

}