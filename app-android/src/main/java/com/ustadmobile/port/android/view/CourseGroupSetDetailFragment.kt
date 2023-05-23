package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseGroupMemberPersonDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.CourseGroupSetDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseGroupSetDetailView
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface CourseGroupMemberPersonDetailFragmentEventHandler {

}

class CourseGroupSetDetailFragment: UstadDetailFragment<CourseGroupSet>(), CourseGroupSetDetailView, CourseGroupMemberPersonDetailFragmentEventHandler {

    private var repo: UmAppDatabase? = null
    private var memberListAdapter: CourseGroupMemberPersonListRecyclerAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null
    private var mBinding: FragmentCourseGroupMemberPersonDetailBinding? = null

    private var mPresenter: CourseGroupSetDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseGroupMemberPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_groupset_detail_overview)

        memberListAdapter = CourseGroupMemberPersonListRecyclerAdapter()

        detailMergerRecyclerView?.adapter = memberListAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter = CourseGroupSetDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var memberList:  List<CourseGroupMemberPerson>? = null
        set(value) {
            field = value
            memberListAdapter?.submitList(value)
        }

    override var entity: CourseGroupSet? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.cgsName
        }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetDetailScreen(
    uiState: CourseGroupSetDetailUiState
){
    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        (1..(uiState.courseGroupSet?.cgsTotalGroups ?: 1)).map{ group ->

            item {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = "${stringResource(id = R.string.group)} ${group}",
                    style = Typography.body1
                )
            }

            items(uiState.membersList.filter { it.cgm.cgmGroupNumber == group }, itemContent = {
                ListItem(
                    text = {
                        Text(text = "${it.name}")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null
                        )
                    }
                )
            })
        }

    }
}

@Composable
@Preview
fun CourseGroupSetDetailScreenPreview(){
    CourseGroupSetDetailScreen(
        uiState = CourseGroupSetDetailUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "Group 1"
                cgsTotalGroups = 4
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    )
}