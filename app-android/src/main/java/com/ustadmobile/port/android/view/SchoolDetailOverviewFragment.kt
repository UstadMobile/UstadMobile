package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolOverviewBinding
import com.toughra.ustadmobile.databinding.ItemClazzSimpleDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.SchoolDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.core.R as CR

interface SchoolDetailOverviewEventListener {
    fun onClickSchoolCode(code: String?)
}

class SchoolDetailOverviewFragment: UstadDetailFragment<SchoolWithHolidayCalendar>(),
        SchoolDetailOverviewView, SchoolDetailOverviewEventListener,
        Observer<PagedList<ClazzWithListDisplayDetails>>{

    private var mBinding: FragmentSchoolOverviewBinding? = null

    private var mPresenter: SchoolDetailOverviewPresenter? = null

    private var clazzRecyclerAdapter: ClazzRecyclerAdapter? = null

    private var clazzRecyclerView : RecyclerView? = null

    protected var currentLiveData: LiveData<PagedList<ClazzWithListDisplayDetails>>? = null

    private val accountManager: UstadAccountManager by instance()

    private val repo: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    private val clazzObserver = Observer<List<ClazzWithListDisplayDetails>?>{
        t -> clazzRecyclerAdapter?.submitList(t)
    }

    override var schoolClazzes: DataSource.Factory<Int, ClazzWithListDisplayDetails>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(this)
            currentLiveData = value?.asRepositoryLiveData(repo)
            currentLiveData?.observe(this, this)
        }

    class ClazzRecyclerAdapter(var presenter: SchoolDetailOverviewPresenter?)
        : ListAdapter<ClazzWithListDisplayDetails,
            ClazzRecyclerAdapter.ClazzViewHolder>(DIFF_CALLBACK_CLAZZ) {

        class ClazzViewHolder(val binding: ItemClazzSimpleDetailBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzViewHolder {
            return ClazzViewHolder(ItemClazzSimpleDetailBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ClazzViewHolder, position: Int) {
            holder.binding.clazz = getItem(position)
            holder.binding.mPresenter = presenter
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolOverviewBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.fragmentEventHandler = this
        }

        clazzRecyclerView = rootView.findViewById(R.id.fragment_school_detail_overview_detail_clazz_rv)

        mPresenter = SchoolDetailOverviewPresenter(requireContext(), arguments.toStringMap(),
                this,  di, viewLifecycleOwner).withViewLifecycle()

        clazzRecyclerAdapter = ClazzRecyclerAdapter(mPresenter)
        clazzRecyclerView?.adapter = clazzRecyclerAdapter
        clazzRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    SchoolDetailOverviewScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        clazzRecyclerView = null
        clazzRecyclerAdapter = null
    }

    override var entity: SchoolWithHolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            mBinding?.schoolWithHolidayCalendar = value
        }

    override var schoolCodeVisible: Boolean
        get() = mBinding?.schoolCodeVisible ?: false
        set(value) {
            mBinding?.schoolCodeVisible = value
        }

    override fun onChanged(value: PagedList<ClazzWithListDisplayDetails>) {
        clazzRecyclerAdapter?.submitList(value)
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onClickSchoolCode(code: String?) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", code)))
        showSnackBar(requireContext().getString(CR.string.copied_to_clipboard))
    }

    companion object{
        val DIFF_CALLBACK_CLAZZ = object: DiffUtil.ItemCallback<ClazzWithListDisplayDetails>() {
            override fun areItemsTheSame(oldItem: ClazzWithListDisplayDetails, newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithListDisplayDetails,
                                            newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem == newItem
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SchoolDetailOverviewScreen(
    uiState: SchoolDetailOverviewUiState = SchoolDetailOverviewUiState(),
    onClickSchoolCode: () -> Unit = {},
    onClickSchoolPhoneNumber: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
    onClickClazz: (Clazz) -> Unit = {},
) {
    LazyColumn {

        if (uiState.schoolDescVisible){
           item {
               Text(
                   modifier = Modifier.padding(all = 16.dp),
                   text = uiState.entity?.schoolDesc ?: "",
                   style = Typography.h6
               )
           }
        }

        if (uiState.schoolCodeLayoutVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolCode ?: "",
                    labelText = stringResource(id = CR.string.school_code),
                    imageId = R.drawable.ic_login_24px,
                    onClick = onClickSchoolCode
                )
            }
        }

        if (uiState.schoolAddressVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolAddress ?: "",
                    labelText = stringResource(id = CR.string.address),
                    imageId = R.drawable.ic_location_pin_24dp
                )
            }
        }

        if (uiState.schoolPhoneNumberVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolPhoneNumber ?: "",
                    labelText = stringResource(id = CR.string.phone_number),
                    imageId = R.drawable.ic_call_bcd4_24dp,
                    onClick = onClickSchoolPhoneNumber,
                    secondaryActionContent = {
                        IconButton(
                            onClick = onClickSms,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Message,
                                contentDescription = stringResource(id = CR.string.message),
                            )
                        }
                    }
                )
            }
        }

        if (uiState.calendarUidVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.holidayCalendar?.umCalendarName ?: "",
                    labelText = stringResource(id = CR.string.holiday_calendar),
                    imageId = R.drawable.ic_perm_contact_calendar_black_24dp
                )
            }
        }

        if (uiState.schoolEmailAddressVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolAddress ?: "",
                    labelText = stringResource(id = CR.string.email),
                    imageId = R.drawable.ic_email_black_24dp,
                    onClick = onClickEmail
                )
            }
        }

        if (uiState.schoolTimeZoneVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolTimeZone ?: "",
                    labelText = stringResource(id = CR.string.timezone),
                    imageId = R.drawable.ic_language_blue_grey_600_24dp,
                )
            }
        }

        item {
            Text(text = stringResource(id = CR.string.courses),
                style = Typography.h6
            )
        }


        items(
            items = uiState.clazzes,
            key = { clazz -> clazz.clazzUid }
        ){ clazz ->
            ListItem(
                modifier = Modifier.clickable {
                    onClickClazz(clazz)
                },
                text = { Text(clazz.clazzName ?: "") },
                secondaryText = { Text(clazz.clazzDesc ?: "") }
            )
        }
    }
}

@Composable
@Preview
fun SchoolDetailOverviewScreenPreview() {
    val uiStateVal = SchoolDetailOverviewUiState(
        entity = SchoolWithHolidayCalendar().apply {
            schoolDesc = "School description over here."
            schoolCode = "abc123"
            schoolAddress = "Nairobi, Kenya"
            schoolPhoneNumber = "+971 44311111"
            schoolGender = 1
            schoolHolidayCalendarUid = 1
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "Kenya calendar A"
            }
            schoolEmailAddress = "info@schoola.com"
            schoolTimeZone = "Asia/Dubai"
        },
        schoolCodeVisible = true,
        clazzes = listOf(
            ClazzWithListDisplayDetails().apply {
                clazzUid = 0L
                clazzName = "Class A"
                clazzDesc = "Class description"
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 1L
                clazzName = "Class B"
                clazzDesc = "Class description"
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 2L
                clazzName = "Class C"
                clazzDesc = "Class description"
            }
        )
    )

    MdcTheme {
        SchoolDetailOverviewScreen(uiStateVal)
    }
}