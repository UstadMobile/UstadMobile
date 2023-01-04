package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseDiscussionDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.CourseDiscussionDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import android.text.format.DateFormat
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.viewmodel.CourseDiscussionDetailUiState
import com.ustadmobile.door.util.systemTimeInMillis

import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadMessageField
import java.util.*


class CourseDiscussionDetailFragment: UstadDetailFragment<CourseDiscussion>(),
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



@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CourseDiscussionDetailScreen(
    uiState: CourseDiscussionDetailUiState = CourseDiscussionDetailUiState(),
    onClickAddPost: () -> Unit = {},
    onClickPost: (DiscussionPostWithDetails) -> Unit = {},
    onClickDeletePost: (DiscussionPostWithDetails) -> Unit = {},
){

    LazyColumn{

        //Description:
        item{

            Text(
                uiState.courseDiscussion?.courseDiscussionDesc?:"",
                style = Typography.body1,
                modifier = Modifier.padding(8.dp)
            )
        }

        item{
            Spacer(modifier = Modifier.height(10.dp))
        }

        item{
            Text(stringResource(R.string.posts),
                style = Typography.h4,
                modifier = Modifier.padding(8.dp))
        }


        items(
            items = uiState.posts,
            key = { post -> post.discussionPostUid }
        ){ post ->

            val context = LocalContext.current
            val datePosted = remember { DateFormat.getDateFormat(context)
                .format(Date(post.discussionPostStartDate ?: 0)).toString() }

            ListItem(
                modifier = Modifier.clickable {
                    onClickPost(post)
                },
                icon = {
                    //TODO: replace with avatar
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null
                    )
                },
                text = {
                    Text(post.discussionPostMessage ?: "" )
                },
                secondaryText = {
                    Column {
                        Text(post.discussionPostTitle?: "")
                        Text(post.postLatestMessage?: "")
                    }
                },
                singleLineSecondaryText = false,
                trailing = {
                    Column {
                        Text(datePosted)
                        Text(stringResource(R.string.num_replies, post.postRepliesCount))
                    }
                }
            )
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
                discussionPostUid = 0L
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
                discussionPostUid = 1L
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