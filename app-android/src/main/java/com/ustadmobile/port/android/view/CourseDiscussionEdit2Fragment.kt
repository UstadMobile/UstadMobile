package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseDiscussionCourseBlockEditBinding
import com.ustadmobile.core.controller.CourseDiscussionEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseDiscussionEditView
import com.ustadmobile.door.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.viewmodel.CourseDiscussionBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseDiscussionDetailUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadMessageField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import java.util.*

class CourseDiscussionEdit2Fragment: UstadEditFragment<CourseBlockWithEntity>(),
    CourseDiscussionEditView {

    private var mBinding: FragmentCourseDiscussionCourseBlockEditBinding? = null

    private var mPresenter: CourseDiscussionEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlockWithEntity>?
        get() = mPresenter

    private var topicListRecyclerAdapter: DiscussionTopicDraggableRecyclerAdapter? = null

    private var topicListRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseDiscussionCourseBlockEditBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
        }

        topicListRecyclerView =
            rootView.findViewById(R.id.fragment_course_discussion_course_block_edit_topic_list_rv)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_discussion, R.string.edit_discussion)

        mPresenter = CourseDiscussionEditPresenter(requireContext(),
            arguments.toStringMap(), this, viewLifecycleOwner, di).withViewLifecycle()

        topicListRecyclerAdapter = DiscussionTopicDraggableRecyclerAdapter(mPresenter,
            mBinding?.fragmentCourseDiscussionCourseBlockEditTopicListRv)

        topicListRecyclerView?.adapter = topicListRecyclerAdapter
        topicListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        topicListRecyclerView = null
        topicListRecyclerAdapter = null
    }

    override var loading: Boolean = false

    override var entity: CourseBlockWithEntity? = null
        get() = field
        set(value) {
            field = value
            mBinding?.block = value
        }
    override var blockTitleError: String? = null
        set(value) {
            field = value
            mBinding?.blockTitleError = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var startDate: Long
        get() = mBinding?.startDate ?: 0
        set(value) {
            mBinding?.startDate = value
        }

    override var startTime: Long
        get() = mBinding?.startTime ?: 0
        set(value) {
            mBinding?.startTime = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone = value
            field = value
        }

    private val topicListObserver = Observer<List<DiscussionTopic>> {
            t -> topicListRecyclerAdapter?.dataSet = t
    }


    override var topicList: MutableLiveData<List<DiscussionTopic>>? = null
        set(value) {
            field?.removeObserver(topicListObserver)
            field = value
            value?.observe(this, topicListObserver)
        }

}

@Composable
private fun PostsDetail(
    posts: List<DiscussionPostWithDetails> = emptyList(),
    onClickPost: (DiscussionPostWithDetails) -> Unit = {}
){

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = LazyListState(),
    ) {
        itemsIndexed(posts) { _, item ->
            PostDetailItem(item)
        }
    }

}

@Composable
private fun PostDetailItem(post: DiscussionPostWithDetails){
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        val context = LocalContext.current
        val datePosted = remember { DateFormat.getDateFormat(context)
            .format(Date(post.discussionPostStartDate ?: 0)).toString() }

        val fullName= post.authorPersonFirstNames + " " + post.authorPersonLastName
        UstadMessageField(
            imageId = R.drawable.ic_person_black_24dp,
            mainText = fullName,
            secondText = post.discussionPostTitle ?: "",
            thirdText = post.postLatestMessage ?: "",
            fourthText = datePosted,
            fifthText = post.postRepliesCount.toString() + " replies",
            thirdTextIcon = R.drawable.ic_baseline_sms_24,
            secondaryActionContent = {
                IconButton(
                    onClick = {  },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                    )
                }
            },
        )
    }
}


@Composable
private fun AddPostButton(onClick: () -> Unit) {
    Button(
        shape = RoundedCornerShape(50),
        onClick = {onClick()},
        modifier = Modifier
            .padding(12.dp)
            .height(45.dp)
            .width(120.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.secondaryColor),
            contentColor = Color.Transparent,
            disabledBackgroundColor = Color.Transparent,),
    ) {
        Row (
            horizontalArrangement = Arrangement.End) {
            Image(
                painter = painterResource(id = R.drawable.ic_add_black_24dp),
                contentDescription = null,
                modifier = Modifier.width(25.dp),
                colorFilter = ColorFilter.tint(color = Color.Black))
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.post),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = Typography.h5
            )
        }
    }
}


@Composable
private fun CourseDiscussionEditScreen(
    uiState: CourseDiscussionBlockEditUiState = CourseDiscussionBlockEditUiState(),
    onClickAddPost: () -> Unit = {},
    onClickPost: (DiscussionPostWithDetails) -> Unit = {},
    onClickDeletePost: (DiscussionPostWithDetails) -> Unit = {},
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        UstadTextEditField(
            value = uiState.courseDiscussion?.courseDiscussionTitle ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.courseDiscussionTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
//                onContentChanged(uiState.discussionPost?.shallowCopy {
//                    discussionPostTitle = it
//                })
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        UstadTextEditField(
            value = uiState.courseDiscussion?.courseDiscussionDesc ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.courseDiscussionDescError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
//                onContentChanged(uiState.discussionPost?.shallowCopy {
//                    discussionPostTitle = it
//                })
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.posts),
            style = Typography.h4,
            modifier = Modifier.padding(8.dp))


        Box(modifier = Modifier.weight(1f)){
            PostsDetail(uiState.posts, onClickPost)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ){
            AddPostButton(onClickAddPost)
        }

    }

}

@Composable
@Preview
fun CourseDiscussionEditScreenPreview(){
    val uiState = CourseDiscussionBlockEditUiState(
        courseDiscussion = CourseDiscussion().apply {
            courseDiscussionTitle = "Discussions on Module 4: Statistics and Data Science"
            courseDiscussionDesc = "Any discussion related to Module 4 of Data Science chapter goes here."
            courseDiscussionActive = true

        },
        posts = listOf(
            DiscussionPostWithDetails().apply {
                discussionPostTitle = "Can I join after week 2?"
                discussionPostMessage = "Iam late to class, CAn I join after?"
                discussionPostVisible = true
                postRepliesCount = 4
                postLatestMessage = "Just make sure you submit a late assignment."
                authorPersonFirstNames = "Mike"
                authorPersonLastName = "Jones"
                discussionPostStartDate = systemTimeInMillis()
            },
            DiscussionPostWithDetails().apply {
                discussionPostTitle = "How to install xlib?"
                discussionPostMessage = "Which version of python do I need?"
                discussionPostVisible = true
                postRepliesCount = 2
                postLatestMessage = "I have the same question"
                authorPersonFirstNames = "Bodium"
                authorPersonLastName = "Carafe"
                discussionPostStartDate = systemTimeInMillis()
            }

        ),

        )

    MdcTheme{
        CourseDiscussionEditScreen(uiState)
    }
}