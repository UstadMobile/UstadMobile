package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemLanguageListBinding
import com.ustadmobile.core.controller.LanguageListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.core.viewmodel.LanguageListUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class LanguageListFragment(): UstadListViewFragment<Language, Language>(),
        LanguageListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: LanguageListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in Language>?
        get() = mPresenter

    class LanguageListViewHolder(val itemBinding: ItemLanguageListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class LanguageListRecyclerAdapter(var presenter: LanguageListPresenter?)
        : SelectablePagedListAdapter<Language, LanguageListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageListViewHolder {
            val itemBinding = ItemLanguageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return LanguageListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: LanguageListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.language = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = LanguageListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = LanguageListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_a_new_language),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.language)
        ustadFragmentTitle = requireContext().getString(R.string.languages)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickCreateNewFab()
        else
            super.onClick(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.languageDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Language> = object
            : DiffUtil.ItemCallback<Language>() {
            override fun areItemsTheSame(oldItem: Language,
                                         newItem: Language): Boolean {
                return oldItem.langUid == newItem.langUid
            }

            override fun areContentsTheSame(oldItem: Language,
                                            newItem: Language): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanguageListScreen(
    uiState: LanguageListUiState,
    onListItemClick: (Language) -> Unit = {},
    onClickSort: () -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        item {
            UstadListSortHeader(
                activeSortOrderOption = uiState.sortOrder,
                onClickSort = onClickSort
            )
        }

        items(
            uiState.languageList,
            key = {
                it.langUid
            }
        ){  language ->
            ListItem(
                modifier = Modifier
                    .clickable {
                        onListItemClick(language)
                    },
                text = { Text(text = language.name ?: "")},
                secondaryText = { Text(text = "${language.iso_639_1_standard} / ${language.iso_639_2_standard}")}
            )
        }
    }
}

@Composable
@Preview
fun LanguageListScreenPreview(){
    LanguageListScreen(
        uiState = LanguageListUiState(
            languageList = listOf(
                Language().apply {
                    name = "Farsi"
                    iso_639_1_standard = "fa"
                    iso_639_2_standard = "far"
                    langUid = 5
                },
                Language().apply {
                    name = "English"
                    iso_639_1_standard = "en"
                    iso_639_2_standard = "eng"
                    langUid = 6
                }
            )
        )
    )
}