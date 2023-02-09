package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemStatementSessionDetailListBinding
import com.ustadmobile.core.controller.StatementListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.editIconId
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.StatementListView
import com.ustadmobile.core.viewmodel.StatementListUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.StatementListViewFragment.Companion.VERB_ICON_MAP
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import java.util.*


class StatementListViewFragment(): UstadListViewFragment<StatementWithSessionDetailDisplay, StatementWithSessionDetailDisplay>(),
        StatementListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: StatementListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in StatementWithSessionDetailDisplay>?
        get() = mPresenter

    class StatementWithSessionsDetailListRecyclerAdapter(var presenter: StatementListPresenter?): SelectablePagedListAdapter<StatementWithSessionDetailDisplay, StatementWithSessionsDetailListRecyclerAdapter.StatementWithSessionDetailListViewHolder>(DIFF_CALLBACK) {

        class StatementWithSessionDetailListViewHolder(val itemBinding: ItemStatementSessionDetailListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatementWithSessionDetailListViewHolder {
            val itemBinding = ItemStatementSessionDetailListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return StatementWithSessionDetailListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: StatementWithSessionDetailListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.statement = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = StatementListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = StatementWithSessionsDetailListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter()
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = false
    }

    override var personWithContentTitle: String? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.statementDao

    companion object {

        @JvmField
        val VERB_ICON_MAP = mapOf(
                VerbEntity.VERB_COMPLETED_UID.toInt() to R.drawable.verb_complete,
                VerbEntity.VERB_PROGRESSED_UID.toInt() to R.drawable.verb_progress,
                VerbEntity.VERB_ATTEMPTED_UID.toInt() to R.drawable.verb_attempt,
                VerbEntity.VERB_INTERACTED_UID.toInt() to R.drawable.verb_interactive,
                VerbEntity.VERB_ANSWERED_UID.toInt() to R.drawable.verb_answered,
                VerbEntity.VERB_SATISFIED_UID.toInt() to R.drawable.verb_passed,
                VerbEntity.VERB_PASSED_UID.toInt() to R.drawable.verb_passed,
                VerbEntity.VERB_FAILED_UID.toInt() to R.drawable.verb_failed)

        val DIFF_CALLBACK: DiffUtil.ItemCallback<StatementWithSessionDetailDisplay> = object
            : DiffUtil.ItemCallback<StatementWithSessionDetailDisplay>() {
            override fun areItemsTheSame(oldItem: StatementWithSessionDetailDisplay,
                                         newItem: StatementWithSessionDetailDisplay): Boolean {
                return oldItem.statementUid == newItem?.statementUid
            }

            override fun areContentsTheSame(oldItem: StatementWithSessionDetailDisplay,
                                            newItem: StatementWithSessionDetailDisplay): Boolean {
                return oldItem == newItem
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StatementListScreen(
    uiState: StatementListUiState = StatementListUiState(),
    onClickStatement: (StatementWithSessionDetailDisplay) -> Unit = {},
) {
    LazyColumn (
       modifier = Modifier
           .fillMaxSize()
           .defaultScreenPadding()
    ){

        items(
            items = uiState.statementList,
            key = { statement -> statement.statementUid }
        ){ statement ->

            val  statementUiState = statement.listItemUiState

            ListItem(
                modifier = Modifier.clickable {
                    onClickStatement(statement)
                },
                text = {
                    Text(statementUiState.personVerbTitleText ?: "",
                        textAlign = TextAlign.End)
                },
                secondaryText = {
                    SecondaryTextContent(statement)
                },
                trailing = {
                    Icon(
                        painterResource(
                            id = VERB_ICON_MAP[statement.statementVerbUid.toInt()]
                                ?: R.drawable.verb_interactive),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun SecondaryTextContent(
    statement: StatementWithSessionDetailDisplay
){

    val  statementUiState = statement.listItemUiState
    val  dateTimeFormatter = rememberFormattedDateTime(
        timeInMillis = statement.timestamp,
        timeZoneId = TimeZone.getDefault().id
    )

    Column (
       horizontalAlignment = Alignment.End
    ){
        if (statementUiState.descriptionVisible){
            Text(statement.objectDisplay ?: "")
        }

        if (statementUiState.questionAnswerVisible){
            Text(statement.objectDisplay ?: "")
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .defaultItemPadding(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("1 hour 30 mins")
            Icon(Icons.Outlined.Timer, contentDescription = "")

            Box(modifier = Modifier.width(8.dp))

            Text(dateTimeFormatter)
            Icon(Icons.Outlined.CalendarToday, contentDescription = "")
        }

        if (statementUiState.resultScoreMaxVisible){
            Row(
                modifier = Modifier.fillMaxWidth()
                    .defaultItemPadding(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(statementUiState.scoreResultsText)
                Text(stringResource(id = R.string.percentage_score,
                    (statement.resultScoreScaled * 100))
                )
                Icon(Icons.Default.Check, contentDescription = "")
            }
        }
    }
}

@Composable
@Preview
fun StatementListScreenPreview() {

    val uiState = StatementListUiState(
        statementList = listOf(
            StatementWithSessionDetailDisplay().apply {
                statementVerbUid = VerbEntity.VERB_COMPLETED_UID
                verbDisplay = "Answered"
                objectDisplay = "object Display"
                resultScoreMax = 90
                resultScoreScaled = 10F
                resultScoreRaw = 70
            }
        )
    )
    MdcTheme {
        StatementListScreen(uiState)
    }
}