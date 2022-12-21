package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseDiscussionDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.CourseDiscussionDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.core.viewmodel.CourseDiscussionDetailUiState
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.core.viewmodel.PersonDetailViewModel
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadMessageField
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.util.*


class CourseDiscussionDetail2Fragment: UstadDetailFragment<CourseDiscussion>(),
    CourseDiscussionDetailView {



    private var mBinding: FragmentCourseDiscussionDetailBinding? = null

    private var mPresenter: CourseDiscussionDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null


    private var descriptionRecyclerAdapter: CourseDiscussionDescriptionRecyclerAdapter? = null

    private var topicsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null

    private var topicsLiveData: LiveData<PagedList<DiscussionTopicListDetail>>? = null


    private var repo: UmAppDatabase? = null


    private var topicsRecyclerAdapter: DiscussionTopicRecyclerAdapter? = null

    private val topicsObserver = Observer<PagedList<DiscussionTopicListDetail>?> {
            t -> topicsRecyclerAdapter?.submitList(t)
    }



    override var topics: DataSourceFactory<Int, DiscussionTopicListDetail>? = null
        set(value) {

            topicsLiveData?.removeObserver(topicsObserver)
            field = value
            val topicsDao = repo?.discussionTopicDao ?:return
            topicsLiveData = value?.asRepositoryLiveData(topicsDao)
            topicsLiveData?.observe(this, topicsObserver)
        }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        mBinding = FragmentCourseDiscussionDetailBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
            it.fragmentCourseDiscussionDetailRv

        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_discussion_detail_rv)

        // 1
        descriptionRecyclerAdapter = CourseDiscussionDescriptionRecyclerAdapter()

        // 2
        topicsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(getString(R.string.topics))
        topicsHeadingRecyclerAdapter?.visible = true




        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
        mPresenter = CourseDiscussionDetailPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        // 3
        topicsRecyclerAdapter = DiscussionTopicRecyclerAdapter(mPresenter)


        detailMergerRecyclerAdapter = ConcatAdapter(descriptionRecyclerAdapter,
            topicsHeadingRecyclerAdapter, topicsRecyclerAdapter)

        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())



        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null

        descriptionRecyclerAdapter = null
        topicsHeadingRecyclerAdapter = null
        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null



    }


    override var entity: CourseDiscussion? = null
        set(value) {
            field = value
            descriptionRecyclerAdapter?.courseDiscussion = value
            ustadFragmentTitle = value?.courseDiscussionTitle
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
            thirdTextIcon = R.drawable.ic_baseline_sms_24
        )
    }
}


@Composable
private fun CourseDiscussionDetailScreen(
    uiState: CourseDiscussionDetailUiState = CourseDiscussionDetailUiState(),
    onClickAddPost: () -> Unit = {},
    onClickPost: (DiscussionPostWithDetails) -> Unit = {},
    onClickDeletePost: (DiscussionPostWithDetails) -> Unit = {},
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            uiState.courseDiscussion?.courseDiscussionTitle?:"",
            style = Typography.body1,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            uiState.courseDiscussion?.courseDiscussionDesc?:"",
            style = Typography.body1,
            modifier = Modifier.padding(8.dp)
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
fun CourseDiscussionDetailScreenPreview(){
    val uiState = CourseDiscussionDetailUiState(
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
        CourseDiscussionDetailScreen(uiState)
    }
}