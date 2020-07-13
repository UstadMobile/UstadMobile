package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAccountListBinding
import com.toughra.ustadmobile.databinding.ItemAccountAboutBinding
import com.toughra.ustadmobile.databinding.ItemAccountListBinding
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import org.kodein.di.instance

class AccountListFragment : UstadBaseFragment(), AccountListView, View.OnClickListener {

    private var mBinding: FragmentAccountListBinding? = null

    private var mCurrentStoredAccounts: List<UmAccount>? = null

    private var mActiveAccount: UmAccount? = null

    class AccountAdapter(var mPresenter: AccountListPresenter?):
            ListAdapter<UmAccount, AccountAdapter.AccountViewHolder>(DIFF_CALLBACK_ACCOUNT){

        class AccountViewHolder(val binding: ItemAccountListBinding): RecyclerView.ViewHolder(binding.root)


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
            val mBinding = ItemAccountListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                presenter = mPresenter
            }
            return AccountViewHolder(mBinding)
        }

        override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
            holder.binding.umaccount = getItem(position)
            holder.binding.activeAccount = position == 0
            holder.binding.logoutBtnVisibility = if(currentList.size == 1 || currentList.isEmpty())
                View.GONE else View.VISIBLE
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mPresenter = null
        }
    }


    class AboutItemAdapter(private val mVersionText:String, var mPresenter: AccountListPresenter?):
            SingleItemRecyclerViewAdapter<AboutItemAdapter.AboutAccountViewHolder>(true){

        class AboutAccountViewHolder(val binding: ItemAccountAboutBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutAccountViewHolder {
            val mBinding = ItemAccountAboutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                presenter = mPresenter
                versionText = mVersionText
            }
            return AboutAccountViewHolder(mBinding)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mPresenter = null
        }
    }

    private var activeAccountObserver = Observer<UmAccount?> {
        mActiveAccount = it
        updateAccountList()
    }

    private var accountListObserver = Observer<List<UmAccount>?> {
        mCurrentStoredAccounts = it
        updateAccountList()
    }

    private fun updateAccountList() {
        val activeAccountVal = mActiveAccount
        val storedAccountsVal = mCurrentStoredAccounts
        if(activeAccountVal != null && storedAccountsVal != null) {
            val otherAccounts: List<UmAccount> = storedAccountsVal.toMutableList().also { it.remove(activeAccountVal) }
                    .sortedBy { it.username }
            accountAdapter?.submitList(listOf(activeAccountVal) + otherAccounts)
        }
    }


    override var accountListLive: DoorLiveData<List<UmAccount>>? = null
        set(value) {
            field?.removeObserver(accountListObserver)
            field = value
            value?.observe(viewLifecycleOwner, accountListObserver)
        }


    override var activeAccountLive: DoorLiveData<UmAccount>? = null
        set(value) {
            field?.removeObserver(activeAccountObserver)
            field = value
            value?.observe(viewLifecycleOwner, activeAccountObserver)
        }

    override var showGetStarted: Boolean? = null
        set(value) {
            field = value
            if(value != null && value){
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.account_list_dest, true)
                        .build()
                findNavController().navigate(R.id.account_get_started_dest,null, navOptions)
            }
        }


    private var accountAdapter: AccountAdapter ? = null

    private var aboutItemAdapter: AboutItemAdapter? = null

    private var mPresenter: AccountListPresenter? = null

    private var newItemRecyclerViewAdapter: NewItemRecyclerViewAdapter? = null

    private var mergeRecyclerAdapter: MergeAdapter? = null

    override fun onClick(p0: View?) {
        mPresenter?.handleClickAddAccount()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView: View
        mBinding = FragmentAccountListBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mBinding?.accountListRecycler?.layoutManager = LinearLayoutManager(requireContext())
        val impl: UstadMobileSystemImpl by instance()
        mPresenter = AccountListPresenter(requireContext(),arguments.toStringMap(),this, di)

        val versionText = impl.getVersion(requireContext()) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(requireContext()))
        accountAdapter = AccountAdapter(mPresenter)
        aboutItemAdapter = AboutItemAdapter(versionText,mPresenter)

        newItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
                createNewText = String.format(getString(R.string.add_another),
                        getString(R.string.account).toLowerCase()))
        newItemRecyclerViewAdapter?.newItemVisible = true

        mergeRecyclerAdapter = MergeAdapter(accountAdapter,
                newItemRecyclerViewAdapter, aboutItemAdapter)

        mBinding?.accountListRecycler?.adapter = mergeRecyclerAdapter

        mPresenter?.onCreate(savedInstanceState.toStringMap())

        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.accountListRecycler?.adapter = null
        mBinding = null
        accountAdapter = null
        aboutItemAdapter = null
        mergeRecyclerAdapter = null
        mPresenter = null
    }

    companion object {

        val DIFF_CALLBACK_ACCOUNT = object: DiffUtil.ItemCallback<UmAccount>() {
            override fun areItemsTheSame(oldItem: UmAccount, newItem: UmAccount): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: UmAccount, newItem: UmAccount): Boolean {
                return oldItem == newItem
            }
        }

    }

}