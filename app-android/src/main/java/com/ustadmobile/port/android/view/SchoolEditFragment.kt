package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.accompanist.themeadapter.appcompat.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolEditBinding
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.UstadTextEditField

class SchoolEditFragment: UstadEditFragment<SchoolWithHolidayCalendar>(), SchoolEditView{

    private var mBinding: FragmentSchoolEditBinding? = null

    private var mPresenter: SchoolEditPresenter? = null

    private var scopedGrantRecyclerAdapter: ScopedGrantAndNameEditRecyclerViewAdapter? = null

    override val mEditPresenter: UstadEditPresenter<*, SchoolWithHolidayCalendar>?
        get() = mPresenter

    override var scopedGrants: LiveData<List<ScopedGrantAndName>>? = null
        set(value) {
            field?.removeObserver(scopedGrantListObserver)
            field = value
            value?.observe(viewLifecycleOwner, scopedGrantListObserver)
        }

    private val scopedGrantListObserver = Observer<List<ScopedGrantAndName>> {
            t -> scopedGrantRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentSchoolEditBinding.inflate(inflater, container,false).also {
                        rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_a_new_school, R.string.edit_school)
        val navController = findNavController()

        mPresenter = SchoolEditPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        mBinding?.mPresenter = mPresenter

        val permissionList = ScopedGrantEditPresenter.PERMISSION_LIST_MAP[School.TABLE_ID]
            ?: throw IllegalStateException("ScopedGrantEdit permission list not found!")
        scopedGrantRecyclerAdapter = ScopedGrantAndNameEditRecyclerViewAdapter(
            mPresenter?.scopedGrantOneToManyHelper, permissionList)

        mBinding?.schoolEditFragmentPermissionsInc?.itemScopedGrantOneToNRecycler?.apply {
            adapter = scopedGrantRecyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.add_a_new_school, R.string.edit_school)
    }

    override var entity: SchoolWithHolidayCalendar? = null
        set(value) {
            field = value
            mBinding?.school = value
        }

    override var fieldsEnabled: Boolean = false
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object{
        val DIFF_CALLBACK_CLAZZ = object: DiffUtil.ItemCallback<Clazz>() {
            override fun areItemsTheSame(oldItem: Clazz, newItem: Clazz): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: Clazz,
                                            newItem: Clazz): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@Composable
private fun SchoolEditScreen(
    uiState: SchoolEditUiState = SchoolEditUiState(),
    onSchoolChanged: (SchoolWithHolidayCalendar?) -> Unit = {},
    onClickTimeZone: () -> Unit = {},
    onClickHolidayCalendar: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolName ?: "",
                label = stringResource(id = R.string.name),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolName = it
                    })
                },
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolDesc ?: "",
                label = stringResource(id = R.string.description),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolDesc = it
                    })
                },
            )
        }


        item {
            UstadTextEditField(
                value = uiState.entity?.schoolTimeZone ?: "",
                label = stringResource(id = R.string.timezone),
                enabled = uiState.fieldsEnabled,
                readOnly = true,
                onClick = onClickTimeZone,
                onValueChange = {  }
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.holidayCalendar?.umCalendarName ?: "",
                label = stringResource(id = R.string.holiday_calendar),
                enabled = uiState.fieldsEnabled,
                readOnly = true,
                onClick = onClickHolidayCalendar,
                onValueChange = {  }
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolAddress ?: "",
                label = stringResource(id = R.string.address),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolAddress = it
                    })
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolPhoneNumber ?: "",
                label = stringResource(id = R.string.phone_number),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolPhoneNumber = it
                    })
                },
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolEmailAddress ?: "",
                label = stringResource(id = R.string.email),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolEmailAddress = it
                    })
                },
            )
        }
    }
}


@Composable
@Preview
fun SchoolEditScreenPreview() {
    val uiState = SchoolEditUiState(
        entity = SchoolWithHolidayCalendar().apply {
            schoolName = "School A"
            schoolDesc = "This is a test school"
            schoolTimeZone = "Asia/Dubai"
            schoolAddress = "123, Main Street, Nairobi, Kenya"
            schoolPhoneNumber = "+90012345678"
            schoolEmailAddress = "info@schoola.com"
        },
    )
    MdcTheme {
        SchoolEditScreen(uiState)
    }
}