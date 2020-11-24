package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonGroupListPresenter
import com.ustadmobile.core.controller.ProductListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonGroupListView
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


class PersonGroupListFragment(): UstadListViewFragment<PersonGroup, PersonGroupWithMemberCount>(),
        PersonGroupListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: PersonGroupListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonGroupWithMemberCount>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = PersonGroupListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonGroupListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.new_group)
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }


    override fun onClick(view: View?) {
//        activity?.prepareGroupListEditCall {
//            if(it != null) {
//                finishWithResult(it)
//            }
//        }.launchGroupListEdit(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.personGroupDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonGroupWithMemberCount> = object
            : DiffUtil.ItemCallback<PersonGroupWithMemberCount>() {
            override fun areItemsTheSame(oldItem: PersonGroupWithMemberCount,
                                         newItem: PersonGroupWithMemberCount): Boolean {
                return oldItem.groupUid == newItem.groupUid
            }

            override fun areContentsTheSame(oldItem: PersonGroupWithMemberCount,
                                            newItem: PersonGroupWithMemberCount): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?) : PersonGroupListFragment {
            return PersonGroupListFragment().apply {
                arguments = bundle
            }
        }
    }
}