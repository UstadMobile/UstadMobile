package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentListBinding
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentPersonHeaderListBinding
import com.ustadmobile.core.controller.ClazzEnrolmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.locale.entityconstants.ClazzEnrolmentListConstants
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.viewmodel.ClazzEnrolmentListUiState
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDateRange
import com.ustadmobile.port.android.view.composable.UstadQuickActionButton
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter


class ClazzEnrolmentListFragment(): UstadListViewFragment<ClazzEnrolment, ClazzEnrolmentWithLeavingReason>(),
        ClazzEnrolmentListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var clazzHeaderAdapter: SimpleHeadingRecyclerAdapter? = null
    private var profileHeaderAdapter: ClazzEnrolmentProfileHeaderAdapter? = null
    private var enrolmentAdapter: ClazzEnrolmentRecyclerAdapter? = null

    private var mPresenter: ClazzEnrolmentListPresenter? = null

    override var autoMergeRecyclerViewAdapter: Boolean = false

    private var enrolmentListObserver: Observer<PagedList<ClazzEnrolmentWithLeavingReason>>? = null

    private var mEnrolmentListLiveData: LiveData<PagedList<ClazzEnrolmentWithLeavingReason>>? = null

    override val listPresenter: UstadListPresenter<*, in ClazzEnrolment>?
        get() = mPresenter

    private var selectedPersonUid: Long = 0

    class ClazzEnrolmentProfileHeaderAdapter(val personUid: Long, var presenter: ClazzEnrolmentListPresenter?):
            SingleItemRecyclerViewAdapter<ClazzEnrolmentProfileHeaderAdapter.ClazzEnrolmentPersonHeaderViewHolder>(true){

        class ClazzEnrolmentPersonHeaderViewHolder(val itemBinding: ItemClazzEnrolmentPersonHeaderListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentPersonHeaderViewHolder {
            return ClazzEnrolmentPersonHeaderViewHolder(
                    ItemClazzEnrolmentPersonHeaderListBinding.inflate(LayoutInflater
                    .from(parent.context), parent, false).also {
                        it.presenter = presenter
                        it.personUid = personUid
                    })
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentPersonHeaderViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            holder.itemView.tag = personUid
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    class ClazzEnrolmentRecyclerAdapter(var presenter: ClazzEnrolmentListPresenter?):
            SelectablePagedListAdapter<ClazzEnrolmentWithLeavingReason, ClazzEnrolmentRecyclerAdapter
            .ClazzEnrolmentListViewHolder>(DIFF_CALLBACK_ENROLMENT) {

        class ClazzEnrolmentListViewHolder(val itemBinding: ItemClazzEnrolmentListBinding): RecyclerView.ViewHolder(itemBinding.root)

        var isStudentEditVisible = false
        var isTeacherEditVisible = false

        val boundEnrolmentViewHolder = mutableListOf<ClazzEnrolmentListViewHolder>()

        fun hasPermissionToEdit(item: ClazzEnrolmentWithLeavingReason?): Boolean {
            return when (item?.clazzEnrolmentRole) {
                ClazzEnrolment.ROLE_TEACHER -> {
                    isTeacherEditVisible
                }
                ClazzEnrolment.ROLE_STUDENT -> {
                    isStudentEditVisible
                }
                else -> {
                    false
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentListViewHolder {
            val itemBinding = ItemClazzEnrolmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return ClazzEnrolmentListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzEnrolment = item
            holder.itemBinding.isEditVisible = hasPermissionToEdit(item)
            boundEnrolmentViewHolder += holder
        }


        override fun onViewRecycled(holder: ClazzEnrolmentListViewHolder) {
            super.onViewRecycled(holder)
            boundEnrolmentViewHolder -= holder
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
            boundEnrolmentViewHolder.clear()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        selectedPersonUid = arguments?.getString(ARG_PERSON_UID)?.toLong() ?: 0
        mPresenter = ClazzEnrolmentListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        profileHeaderAdapter = ClazzEnrolmentProfileHeaderAdapter(selectedPersonUid, mPresenter)

        clazzHeaderAdapter = SimpleHeadingRecyclerAdapter(getText(R.string.person).toString())
        enrolmentAdapter = ClazzEnrolmentRecyclerAdapter(mPresenter).also {
            enrolmentListObserver = PagedListSubmitObserver(it)
        }
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this)

        mMergeRecyclerViewAdapter = ConcatAdapter(profileHeaderAdapter,clazzHeaderAdapter,
                enrolmentAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.enrolment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        profileHeaderAdapter = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzEnrolmentDao

    override var person: Person? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = person?.personFullName()
        }

    override var clazz: Clazz? = null
        get() = field
        set(value){
            field = value
            val personInClazzStr = requireContext().getString(
                    R.string.person_enrolment_in_class, person?.personFullName(), value?.clazzName)
            clazzHeaderAdapter?.visible = true
            clazzHeaderAdapter?.headingText = personInClazzStr
        }

    override var isStudentEnrolmentEditVisible: Boolean = false
        get() = field
        set(value) {
            field = value
            enrolmentAdapter?.isStudentEditVisible = value
        }

    override var isTeacherEnrolmentEditVisible: Boolean = false
        get() = field
        set(value) {
            field = value
            enrolmentAdapter?.isTeacherEditVisible = value
        }

    override var enrolmentList: DataSource.Factory<Int, ClazzEnrolmentWithLeavingReason>? = null
        get() = field
        set(value) {
            field = value
            val studentObserverVal = enrolmentListObserver ?: return
            val repoDao = displayTypeRepo ?: return
            mEnrolmentListLiveData?.removeObserver(studentObserverVal)
            mEnrolmentListLiveData = value?.asRepositoryLiveData(repoDao)
            mEnrolmentListLiveData?.observe(viewLifecycleOwner, studentObserverVal)
        }

    companion object {

        val DIFF_CALLBACK_ENROLMENT: DiffUtil.ItemCallback<ClazzEnrolmentWithLeavingReason> = object
            : DiffUtil.ItemCallback<ClazzEnrolmentWithLeavingReason>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithLeavingReason,
                                         newItem: ClazzEnrolmentWithLeavingReason): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithLeavingReason,
                                            newItem: ClazzEnrolmentWithLeavingReason): Boolean {
                return oldItem == newItem
            }
        }
    }


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzEnrolmentListScreen(
    uiState: ClazzEnrolmentListUiState,
    onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit = {},
    onViewProfileClick: () -> Unit = {}
){

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ){

        item{
            UstadQuickActionButton(
                labelText = stringResource(id = R.string.view_profile),
                imageId = R.drawable.ic_person_black_24dp,
                onClick = onViewProfileClick
            )
        }

        item {
            Divider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        }

        item{
            Text(
                text = stringResource(id = R.string.person_enrolment_in_class, uiState.personName ?: "", uiState.courseName ?: ""),
                style = Typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }

        items(
            uiState.enrolmentList,
            key = {
                it.clazzEnrolmentUid
            }
        ){  enrolment ->

            val joinedLeftDate = rememberFormattedDateRange(
                startTimeInMillis = enrolment.clazzEnrolmentDateJoined,
                endTimeInMillis = enrolment.clazzEnrolmentDateLeft,
                timeZoneId = "UTC"
            )
            var itemPrimaryText = "${messageIdResource(id = ClazzEnrolmentListConstants.ROLE_TO_MESSAGE_ID_MAP[enrolment.clazzEnrolmentRole] ?: 0)} - ${messageIdResource( id = ClazzEnrolmentListConstants.OUTCOME_TO_MESSAGE_ID_MAP[enrolment.clazzEnrolmentOutcome] ?: 0 )}"

            if (enrolment.leavingReason != null){
                itemPrimaryText = "$itemPrimaryText (${enrolment.leavingReason?.leavingReasonTitle})"
            }

            ListItem(
                text = { Text(text = itemPrimaryText) },
                secondaryText = { Text(text = joinedLeftDate)},
                trailing = {
                    IconButton(
                        onClick = {
                            onEditItemClick(enrolment)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_white_24dp), 
                            contentDescription = stringResource(id = R.string.edit)
                        )
                    }
                }
            )
        }
    }
}

@Composable
@Preview
fun ClazzEnrolmentListPreview(){
    ClazzEnrolmentListScreen(
        uiState = ClazzEnrolmentListUiState(
            personName = "Ahmad",
            courseName = "Mathematics",
            enrolmentList = listOf(
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349880298
                    clazzEnrolmentDateLeft = 509823093
                    clazzEnrolmentUid = 7
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 201
                },
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349887338
                    clazzEnrolmentDateLeft = 409937093
                    clazzEnrolmentUid = 8
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 203
                    leavingReason = LeavingReason().apply {
                        leavingReasonTitle = "transportation problem"
                    }
                }
            )
        )
    )
}