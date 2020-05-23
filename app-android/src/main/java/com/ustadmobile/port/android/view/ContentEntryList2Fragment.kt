package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemContentEntryListBinding
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ContentEntryList2Fragment : UstadListViewFragment<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(),
        ContentEntryList2View, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ContentEntryList2Presenter? = null

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter

    class ContentEntryListViewHolder(val itemBinding: ItemContentEntryListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ContentEntryListRecyclerAdapter(var presenter: ContentEntryList2Presenter?)
        : SelectablePagedListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, ContentEntryListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentEntryListViewHolder {
            val itemBinding = ItemContentEntryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ContentEntryListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ContentEntryListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.contentEntry = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ContentEntryList2Presenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = ContentEntryListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.content_editor_create_new_title))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.content_editor_create_new_title)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.content_entry_add_options_dest, ContentEntry::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.contentEntryDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = object
            : DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>() {
            override fun areItemsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                         newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem.contentEntryUid == newItem.contentEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                            newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun checkAndGetContentType(type: String): String {
        return "${getString(R.string.content_type, context)} (${type.split("/")[1]})"
    }

    override fun checkAndGetContentDrawable(type: String): Int {
        return when {
            type.contains("video") -> {
                R.drawable.ic_video_library_24px
            }
            type.contains("epub") -> {
                R.drawable.ic_book_black_24dp
            }
            else -> {
                R.drawable.ic_audiotrack_24px
            }
        }
    }
}