package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemPersonSessionsListBinding
import com.ustadmobile.core.controller.SessionListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.viewmodel.SessionListUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import java.util.*


class SessionListFragment(): UstadListViewFragment<PersonWithSessionsDisplay, PersonWithSessionsDisplay>(),
        SessionListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SessionListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithSessionsDisplay>?
        get() = mPresenter

    class PersonWithSessionsDisplayListRecyclerAdapter(var presenter: SessionListPresenter?): SelectablePagedListAdapter<PersonWithSessionsDisplay, PersonWithSessionsDisplayListRecyclerAdapter.PersonWithSessionDisplayListViewHolder>(DIFF_CALLBACK) {

        class PersonWithSessionDisplayListViewHolder(val itemBinding: ItemPersonSessionsListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithSessionDisplayListViewHolder {
            val itemBinding = ItemPersonSessionsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return PersonWithSessionDisplayListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonWithSessionDisplayListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SessionListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = PersonWithSessionsDisplayListRecyclerAdapter(mPresenter)
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
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithSessionsDisplay> = object
            : DiffUtil.ItemCallback<PersonWithSessionsDisplay>() {
            override fun areItemsTheSame(oldItem: PersonWithSessionsDisplay,
                                         newItem: PersonWithSessionsDisplay): Boolean {
                return oldItem.contextRegistration == newItem.contextRegistration
            }

            override fun areContentsTheSame(oldItem: PersonWithSessionsDisplay,
                                            newItem: PersonWithSessionsDisplay): Boolean {
                return oldItem == newItem
            }
        }
    }

}

@Composable
private fun SessionListScreen(
    uiState: SessionListUiState = SessionListUiState(),
    onClickPerson: (PersonWithSessionsDisplay) -> Unit = {}
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        items(
            items = uiState.sessionsList,
        ){ personItem ->
            PersonListItem(
                person = personItem,
                onClick = onClickPerson
            )
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonListItem(
    person: PersonWithSessionsDisplay,
    onClick: (PersonWithSessionsDisplay) -> Unit,
){

    val uiState = person.listItemUiState

    val dateTimeFormatted = rememberFormattedDateTime (
        timeInMillis = person.startDate,
        timeZoneId = TimeZone.getDefault().id
    )

    ListItem (
        modifier = Modifier.clickable {
            onClick(person)
        },
        text = {
            Text(text = "Passed - ")
        },
        secondaryText = {
            Column{
                Text(dateTimeFormatted)

                if (uiState.scoreResultVisible){
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null
                        )

                        Text(stringResource(id = R.string.percentage_score,
                            (person.resultScoreScaled * 100))
                        )

                        Text(uiState.scoreResultText)
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null
            )
        }
    )
}

@Composable
@Preview
fun SessionListScreenPreview() {
    MdcTheme {
        SessionListScreen(
            uiState = SessionListUiState(
                sessionsList = listOf(
                    PersonWithSessionsDisplay().apply {
                        startDate = 13
                        resultScoreScaled = 4F
                        resultScore = 5
                        resultMax = 10
                    },
                    PersonWithSessionsDisplay().apply {
                        startDate = 13
                    }
                ),
            )
        )
    }
}