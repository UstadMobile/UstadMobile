package com.ustadmobile.core.viewmodel.discussionpost.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class DiscussionPostEditUiState(
    val discussionPost: DiscussionPost? = null,
    val fieldsEnabled: Boolean = true,
    val discussionPostTitleError: String? = null,
    val discussionPostDescError: String? = null
)

class DiscussionPostEditViewModel (
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadEditViewModel(di, savedStateHandle, destinationName){

    private val _uiState: MutableStateFlow<DiscussionPostEditUiState> =
        MutableStateFlow(
            DiscussionPostEditUiState(
                fieldsEnabled = false,
            )
        )

    val uiState: Flow<DiscussionPostEditUiState> = _uiState.asStateFlow()

    private val discussionPostUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val courseBlockUidArg: Long
        get() = savedStateHandle[ARG_COURSE_BLOCK_UID]?.toLong() ?: 0L

    init {
        loadingState = LoadingUiState.INDETERMINATE

        val title = if(discussionPostUid == 0L)
            systemImpl.getString(MessageID.add_new_post)
        else
            systemImpl.getString(MessageID.edit)

        _appUiState.update {
            AppUiState(
                title = title,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.post),
                    onClick = this::onClickSave
                ),
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            awaitAll(
                async {
                    loadEntity(
                        serializer = DiscussionPost.serializer(),
                        onLoadFromDb = {
                            it.discussionPostDao.takeIf { entityUidArg != 0L }
                                ?.findByUid(entityUidArg)
                        },
                        makeDefault = {
                            DiscussionPost().also {
                                //Any default value does here
                                it.discussionPostUid = activeDb.doorPrimaryKeyManager
                                    .nextIdAsync(DiscussionPost.TABLE_ID)
                                it.discussionPostCourseBlockUid = courseBlockUidArg
                                it.discussionPostArchive = false
                                it.discussionPostStartedPersonUid = accountManager.activeAccount.personUid
                                it.discussionPostStartDate = systemTimeInMillis()
                            }
                        },
                        uiUpdate = { entityToDisplay ->
                            _uiState.update {
                                it.copy(discussionPost = entityToDisplay)
                            }
                        }
                    )
                },

            )


            _uiState.update { prev ->
                prev.copy(

                    fieldsEnabled = true,
                )
            }
            loadingState = LoadingUiState.NOT_LOADING
        }
    }

    fun onEntityChanged(entity: DiscussionPost?) {
        _uiState.update { prev ->
            prev.copy(
                discussionPost = entity,
                discussionPostTitleError = updateErrorMessageOnChange(prev.discussionPost?.discussionPostTitle,
                    entity?.discussionPostTitle, prev.discussionPostTitleError),
                discussionPostDescError = updateErrorMessageOnChange(prev.discussionPost?.discussionPostMessage,
                    entity?.discussionPostMessage, prev.discussionPostDescError)
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = DiscussionPost.serializer(),
            commitDelay = 200
        )
    }

    //Required because Aztec's effect will freeze the copy of the entity,
    fun onDiscussionPostBodyChanged(postBody: String) {
        val entityToSave = _uiState.updateAndGet { prev ->
            prev.copy(
                discussionPost = prev.discussionPost?.shallowCopy {
                    discussionPostMessage = postBody
                }
            )
        }.discussionPost

        scheduleEntityCommitToSavedState(
            entity = entityToSave,
            serializer = DiscussionPost.serializer(),
            commitDelay = 200
        )
    }

    /**
     * On click save post.
     */
    fun onClickSave(){

        val post = _uiState.value.discussionPost ?: return

        _uiState.update { prev ->
            prev.copy(
                discussionPostTitleError = if(post.discussionPostTitle.isNullOrEmpty()){
                    systemImpl.getString(MessageID.field_required_prompt)
                }else{
                    null
                },
                discussionPostDescError =  if(post.discussionPostMessage.isNullOrEmpty()){
                    systemImpl.getString(MessageID.field_required_prompt)
                }else{
                    null
                }
            )
        }

        viewModelScope.launch {
            if(_uiState.value.discussionPostTitleError == null
                && _uiState.value.discussionPostDescError == null){

                activeDb.withDoorTransactionAsync {
                    post.discussionPostClazzUid = activeDb.courseBlockDao
                        .findClazzUidByCourseBlockUid(post.discussionPostCourseBlockUid)

                    activeDb.discussionPostDao.upsertAsync(post)
                }

                finishWithResult(
                    DiscussionPostDetailViewModel.DEST_NAME, post.discussionPostUid, post
                )
            }
        }

    }

    companion object {

        const val DEST_NAME = "DiscussionPostEdit"

    }

}