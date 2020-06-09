package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemLanguageListBinding
import com.ustadmobile.core.controller.LanguageListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
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
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = LanguageListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.language))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.language)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
    /*    if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.language_edit_dest, Language::class.java)*/
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