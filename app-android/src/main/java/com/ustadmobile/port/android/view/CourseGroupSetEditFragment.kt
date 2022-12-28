package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseGroupSetEditBinding
import com.ustadmobile.core.controller.CourseGroupSetEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.core.viewmodel.CourseGroupSetEditUiState
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadTextEditField


interface CourseGroupSetEditFragmentEventHandler {

    fun handleAssignRandomGroupsClicked()

    fun handleNumberOfGroupsChanged(number: Int)
}

class CourseGroupSetEditFragment: UstadEditFragment<CourseGroupSet>(), CourseGroupSetEditView, CourseGroupSetEditFragmentEventHandler {

    private var mBinding: FragmentCourseGroupSetEditBinding? = null

    private var mPresenter: CourseGroupSetEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseGroupSet>?
        get() = mPresenter

    private var headerAdapter: CourseGroupSetHeaderAdapter? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null
    private var courseGroupMemberEditAdapter: CourseGroupMemberEditAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseGroupSetEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_groupset_edit_overview)

        headerAdapter = CourseGroupSetHeaderAdapter(this)

        courseGroupMemberEditAdapter = CourseGroupMemberEditAdapter(this)

        detailMergerRecyclerAdapter = ConcatAdapter(headerAdapter, courseGroupMemberEditAdapter)
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = CourseGroupSetEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: CourseGroupSet? = null
        get() = field
        set(value) {
            field = value
            headerAdapter?.courseGroupSet = value
        }

    override var memberList: List<CourseGroupMemberPerson>? = null
        get() = courseGroupMemberEditAdapter?.currentList ?: listOf()
        set(value){
            field = value
            courseGroupMemberEditAdapter?.submitList(value)
            courseGroupMemberEditAdapter?.notifyDataSetChanged()
        }

    override var groupList: List<IdOption>? = null
        set(value) {
            field = value
            courseGroupMemberEditAdapter?.groupList = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
        }

    override fun handleAssignRandomGroupsClicked() {
        mPresenter?.handleAssignRandomGroupsClicked()
    }

    override fun handleNumberOfGroupsChanged(number: Int) {
        mPresenter?.handleNumberOfGroupsChanged(number)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetEditScreen(
    uiState: CourseGroupSetEditUiState,
    onCourseGroupSetChange: (CourseGroupSet?) -> Unit = {},
    onClickAssign: () -> Unit = {},
    onCgmChange: (CourseGroupMember?) -> Unit = {}
){
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        UstadTextEditField(
            value = uiState.courseGroupSet?.cgsName ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.courseTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCourseGroupSetChange(uiState.courseGroupSet?.shallowCopy{
                    cgsName = it
                })
            }
        )

        UstadTextEditField(
            value = uiState.courseGroupSet?.cgsTotalGroups.toString(),
            label = stringResource(id = R.string.number_of_groups),
            error = uiState.numOfGroupsError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCourseGroupSetChange(uiState.courseGroupSet?.shallowCopy{
                    cgsTotalGroups = it.toInt()
                })
            }
        )

        Button(
            onClick = onClickAssign,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(stringResource(R.string.assign_to_random_groups).uppercase(),
            )
        }

        val groups =  (1 ..(uiState.courseGroupSet?.cgsTotalGroups ?: 1)).map { "Group $it" }

        uiState.membersList.forEach { member ->
            ListItem (
                text = {
                    Text(text = member.name)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                },
                trailing = {
                    UstadExposedDropDownMenuField<String>(
                        value = "Group " + member.cgm.cgmGroupNumber,
                        label = "",
                        options = groups,
                        onOptionSelected = {
                            onCgmChange(member.cgm.shallowCopy{
                                cgmGroupNumber = it.toInt()
                            })
                        },
                        itemText =  { it },
                        modifier = Modifier
                            .width(120.dp)
                    )
                }
            )
        }

    }
}

@Composable
@Preview
fun CourseGroupSetEditScreenPreview(){
    CourseGroupSetEditScreen(
        uiState = CourseGroupSetEditUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "ttl"
                cgsTotalGroups = 6
            },
            membersList = listOf(
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    )
}