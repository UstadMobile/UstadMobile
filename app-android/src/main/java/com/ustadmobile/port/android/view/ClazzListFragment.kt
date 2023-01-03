package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ClazzListUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.composable.UstadListFilterChipsHeader
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ClazzListFragment(): UstadListViewFragment<Clazz, ClazzWithListDisplayDetails>(),
        ClazzList2View, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener,
        BottomSheetOptionSelectedListener{

    private var mPresenter: ClazzListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWithListDisplayDetails>?
        get() = mPresenter

    override var newClazzListOptionVisible: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        mPresenter = ClazzListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_a_new_class),  onClickSort = this,
            sortOrderOption = mPresenter?.sortOptions?.get(0),
            filterOptions = ClazzListPresenter.FILTER_OPTIONS.toListFilterOptions(requireContext(), di),
            onFilterOptionSelected = mPresenter)
        mDataRecyclerViewAdapter = ClazzListRecyclerAdapter(mPresenter, di)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.course)

        //override this to show our own bottom sheet
        fabManager?.onClickListener = {
            val optionList = if(newClazzListOptionVisible) {
                listOf(BottomSheetOption(R.drawable.ic_add_black_24dp,
                        requireContext().getString(R.string.add_a_new_course), NEW_CLAZZ))
            }else {
                listOf()
            } + listOf(BottomSheetOption(R.drawable.ic_login_24px,
                requireContext().getString(R.string.join_existing_course), JOIN_CLAZZ))

            val sheet = OptionsBottomSheetFragment(optionList, this)
            sheet.show(childFragmentManager, sheet.tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        when(optionSelected.optionCode) {
            NEW_CLAZZ -> mPresenter?.handleClickCreateNewFab()
            JOIN_CLAZZ -> mPresenter?.handleClickJoinClazz()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onClick(v: View?) {
        if(v?.id == R.id.item_createnew_layout) {
            var args = bundleOf()
            val filterExcludeMembersOfSchool =
                    arguments?.get(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL)?.toString()?.toLong()?:0L
            if(filterExcludeMembersOfSchool != 0L){
                args = bundleOf(UstadView.ARG_SCHOOL_UID to filterExcludeMembersOfSchool.toString())
            }
            args.putAll(arguments)
            mPresenter?.handleClickAddNewItem(args.toStringMap())
        } else {
            super.onClick(v)
        }
    }

    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: IdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao


    companion object {

        const val NEW_CLAZZ = 2

        const val JOIN_CLAZZ = 3


        @JvmStatic
        val FOREIGNKEYADAPTER_COURSE = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.coursePictureDao.findByClazzUidAsync(foreignKey)?.coursePictureUri
            }
        }

    }


}

@Composable
private fun ClazzListScreen(
    uiState: ClazzListUiState = ClazzListUiState(),
    onClickClazz: (Clazz) -> Unit = {},
    onClickSort: () -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        item {
            UstadListSortHeader(
                activeSortOrderOption = uiState.activeSortOrderOption,
                enabled = uiState.fieldsEnabled,
                onClickSort = onClickSort
            )
        }

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.filterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }

        items(
            items = uiState.clazzList,
            key = { clazz -> clazz.clazzUid }
        ){ clazz ->

            ClazzListItem(
                clazz = clazz,
                onClickClazz = onClickClazz
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzListItem(
    clazz: ClazzWithListDisplayDetails,
    onClickClazz: (Clazz) -> Unit
){

    val role = (RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == clazz.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.messageId ?: MessageID.student)

    ListItem(
        modifier = Modifier.clickable {
            onClickClazz(clazz)
        },
        text = { Text(clazz.clazzName ?: "") },
        secondaryText = {
            Column {
                Text(clazz.clazzDesc ?: "")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lens,
                        contentDescription = "",
                    )
                    Text(
                        text = stringResource(
                            R.string.x_percent_attended,
                            clazz.attendanceAverage * 100
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "",
                    )
                    Text(
                        text = stringResource(
                            R.string.x_teachers_y_students,
                            clazz.numTeachers, clazz.numStudents,
                        )
                    )
                }
            }
        },
        trailing = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Badge,
                    contentDescription = "",
                )
                Text(messageIdResource(id = role))
            }
        },
    )
}

@Composable
@Preview
fun ClazzListScreenPreview() {
    val uiStateVal = ClazzListUiState(
        clazzList = listOf(
            ClazzWithListDisplayDetails().apply {
                clazzUid = 1
                clazzName = "Class Name"
                clazzDesc = "Class Description"
                attendanceAverage = 0.3F
                numTeachers = 3
                numStudents = 2
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 2
                clazzName = "Class Name"
                clazzDesc = "Class Description"
                attendanceAverage = 0.3F
                numTeachers = 3
                numStudents = 2
            }
        ),
        filterOptions = listOf(
            MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
            MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
            MessageIdOption2(MessageID.all, 0),
        )
    )

    MdcTheme {
        ClazzListScreen(uiStateVal)
    }
}