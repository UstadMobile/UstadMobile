package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemVerbEntityListBinding
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.controller.VerbEntityListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.VerbEntityListView
import com.ustadmobile.lib.db.entities.VerbDisplay
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class VerbEntityListFragment() : UstadListViewFragment<VerbDisplay, VerbDisplay>(),
        VerbEntityListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var mPresenter: VerbEntityListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in VerbDisplay>?
        get() = mPresenter

    class VerbEntityListViewHolder(val itemBinding: ItemVerbEntityListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class VerbEntityListRecyclerAdapter(var presenter: VerbEntityListPresenter?)
        : SelectablePagedListAdapter<VerbDisplay, VerbEntityListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerbEntityListViewHolder {
            val itemBinding = ItemVerbEntityListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return VerbEntityListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: VerbEntityListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.verbEntity = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = VerbEntityListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        mDataRecyclerViewAdapter = VerbEntityListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_a_new_verb))
        return view
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.verbDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<VerbDisplay> = object
            : DiffUtil.ItemCallback<VerbDisplay>() {
            override fun areItemsTheSame(oldItem: VerbDisplay,
                                         newItem: VerbDisplay): Boolean {
                return oldItem.verbUid == newItem.verbUid
            }

            override fun areContentsTheSame(oldItem: VerbDisplay,
                                            newItem: VerbDisplay): Boolean {
                return oldItem == newItem
            }
        }
    }
}