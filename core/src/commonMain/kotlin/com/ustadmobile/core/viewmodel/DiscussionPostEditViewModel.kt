package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionPost
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
): UstadEditViewModel(di, savedStateHandle, DiscussionPostEditView.VIEW_NAME){

    private val _uiState: MutableStateFlow<DiscussionPostEditUiState> = MutableStateFlow(
        DiscussionPostEditUiState(
            fieldsEnabled = false,
        )
    )

    val uiState: Flow<DiscussionPostEditUiState> = _uiState.asStateFlow()

    private val discussionPostUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val discussionBlockUid: Long
        get() = savedStateHandle[UstadView.ARG_BLOCK_UID]?.toLong() ?: 0

    private val clazzUid: Long
        get() = savedStateHandle[ARG_CLAZZUID]?.toLong()?:0L

    init {
        loadingState = LoadingUiState.INDETERMINATE

        val title = if(discussionPostUid == 0L) systemImpl.getString(MessageID.add_new) else systemImpl.getString(
            MessageID.edit)

        _appUiState.update {
            AppUiState(
                title = title,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.save),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {
            awaitAll(
                async {
                    loadEntity(
                        serializer = DiscussionPost.serializer(),
                        onLoadFromDb = {
                            it.discussionPostDao.takeIf { discussionPostUid != 0L }
                                ?.findByUid(discussionPostUid)
                                       },
                        makeDefault = {
                            DiscussionPost().also {
                                //Any default value does here
                                it.discussionPostClazzUid = clazzUid
                                it.discussionPostDiscussionTopicUid = discussionBlockUid
                                //Setting reply to false:
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
            prev.copy(discussionPost = entity)
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = DiscussionPost.serializer(),
            commitDelay = 200
        )
    }

    private fun DiscussionPostEditUiState.hasErrors(): Boolean {
        return discussionPostTitleError != null ||
                discussionPostDescError != null
    }

    /**
     * On click save post.
     */
    fun onClickSave(){

        loadingState = LoadingUiState.INDETERMINATE
        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }
        val requiredFieldMessage = systemImpl.getString(MessageID.field_required_prompt)

        val post = _uiState.value.discussionPost ?: return
        post.discussionPostArchive = false //This denotes that this is not a  reply

        _uiState.update { prev ->
            prev.copy(
                discussionPostTitleError = if(post.discussionPostTitle.isNullOrEmpty()){
                    requiredFieldMessage
                }else{
                    null
                },
                discussionPostDescError =  if(post.discussionPostMessage.isNullOrEmpty()){
                    requiredFieldMessage
                }else{
                    null
                }
            )
        }

        if(_uiState.value.hasErrors()){
            loadingState = LoadingUiState.NOT_LOADING
            _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
            return
        }

        viewModelScope.launch {
            if(_uiState.value.discussionPostTitleError == null
                && _uiState.value.discussionPostDescError == null){

                activeDb.withDoorTransactionAsync {
                    if(entityUidArg == 0L){
                        activeDb.discussionPostDao.insertAsync(post)
                    }else{
                        activeDb.discussionPostDao.updateAsync(post)
                    }
                }
                finishWithResult(post)
            }
        }

    }

}