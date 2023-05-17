package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.util.getSystemTimeInMillis
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.time.Duration.Companion.minutes

class DiscussionPostDetailViewModelTest {

    val endpoint = Endpoint("http://test.com/")

    @Test
    fun givenPostDetails_whenCalled_shouldLoadRepliesToView(){
        testViewModel<DiscussionPostDetailViewModel>(){
            setActiveUser(endpoint)

            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            val post = DiscussionPost().apply {
                discussionPostTitle = "Test post"
                discussionPostMessage = "Test message"
                discussionPostStartDate = getSystemTimeInMillis()
                discussionPostDiscussionTopicUid = 42L
                discussionPostVisible = true
                discussionPostArchive = false
                discussionPostStartedPersonUid = 42L
                discussionPostClazzUid = 21L

            }

            post.discussionPostUid = db.discussionPostDao.insert(post)

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = post.discussionPostUid.toString()
                DiscussionPostDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 2.minutes ){
                it.discussionPost?.discussionPostTitle == "Test post" && it.discussionPost?.discussionPostMessage == "Test message"
                        && it.discussionPost?.discussionPostArchive == false
            }
        }
    }

    @Test
    fun givenPostDetail_whenAddMessage_shouldAddMessage(){
        //TODO
    }

    @Test
    fun givenPostDetail_whenUpdateMessageRead_shouldUpdate(){
        //TODO
    }
}