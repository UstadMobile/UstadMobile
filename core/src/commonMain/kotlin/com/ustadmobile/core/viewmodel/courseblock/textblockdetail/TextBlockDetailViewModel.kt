package com.ustadmobile.core.viewmodel.courseblock.textblockdetail

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazz.launchSetTitleFromClazzUid
import com.ustadmobile.lib.db.composites.CourseBlockAndPicture
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class TextBlockDetailUiState(
    val courseBlock: CourseBlockAndPicture? = null,
)

class TextBlockDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<CourseBlock>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(TextBlockDetailUiState())

    val uiState: Flow<TextBlockDetailUiState> = _uiState.asStateFlow()

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.courseBlockDao().findByUidWithPictureAsFlow(entityUidArg).collect {
                        _uiState.update { prev ->
                            prev.copy(courseBlock = it)
                        }
                    }
                }

                launchSetTitleFromClazzUid(clazzUid) { title ->
                    _appUiState.update { it.copy(title = title) }
                }
            }
        }

    }

    companion object {

        const val DEST_NAME = "CourseText"

    }

}