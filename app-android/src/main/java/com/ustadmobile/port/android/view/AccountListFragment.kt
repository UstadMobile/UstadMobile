package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAccountListBinding
import com.toughra.ustadmobile.databinding.ItemAccountAboutBinding
import com.toughra.ustadmobile.databinding.ItemAccountActiveBinding
import com.toughra.ustadmobile.databinding.ItemAccountListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class AccountListFragment : UstadBaseFragment(), AccountListView, View.OnClickListener {

    private var mBinding: FragmentAccountListBinding? = null

    class ActiveAccountAdapter(var mPresenter: AccountListPresenter?):
            SingleItemRecyclerViewAdapter<ActiveAccountAdapter.ActiveAccountViewHolder>(true),
            Observer<UmAccount>{

        private var activeAccount: UmAccount? = null

        class ActiveAccountViewHolder(val binding: ItemAccountActiveBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveAccountViewHolder {
            val mBinding = ItemAccountActiveBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                presenter = mPresenter
            }
            return ActiveAccountViewHolder(mBinding)
        }

        override fun onBindViewHolder(holder: ActiveAccountViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            updateAccount()
        }

        private fun updateAccount(){
            currentViewHolder?.binding?.account = activeAccount
        }

        override fun onChanged(account: UmAccount?) {
            activeAccount = account
            updateAccount()
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mPresenter = null
            activeAccount = null
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

    class AccountListAdapter(var mPresenter: AccountListPresenter?):
            ListAdapter<UmAccount,AccountListAdapter.AccountListViewHolder>(DIFF_CALLBACK_ACCOUNT),
            Observer<List<UmAccount>>{

        var activeAccount: UmAccount? = null
        set(value){
            updateList()
            field = value
        }

        private var storedAccounts: MutableList<UmAccount>? = null

        class AccountListViewHolder(val binding: ItemAccountListBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountListViewHolder {
            val mBinding = ItemAccountListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                presenter = mPresenter
            }
            return AccountListViewHolder(mBinding)
        }

        override fun onChanged(umAccounts: List<UmAccount>?) {
            storedAccounts = umAccounts as MutableList<UmAccount>?
            updateList()
        }

        private fun updateList(){
            val umAccount = activeAccount
            if(umAccount != null){
                storedAccounts?.remove(umAccount)
            }
            submitList(storedAccounts)
        }


        override fun onBindViewHolder(holderList: AccountListViewHolder, position: Int) {
            holderList.binding.umaccount = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mPresenter = null
            storedAccounts = null
            activeAccount = null
        }
    }


    private val accountListObserver = Observer<List<UmAccount>?> {
        t -> accountListAdapter?.submitList(t)
    }

    private var activeAccountListObserver = Observer<UmAccount?> {
        t -> accountListAdapter?.activeAccount = t
    }


    override var accountListLive: DoorLiveData<List<UmAccount>>? = null
        set(value) {
            field?.removeObserver(accountListObserver)
            field = value
            value?.observe(this, accountListObserver)
        }


    override var activeAccountLive: DoorLiveData<UmAccount>? = null
        set(value) {
            val observer = activeAccountAdapter ?:return
            field?.removeObserver(observer)
            field?.removeObserver(activeAccountListObserver)
            field = value
            value?.observe(viewLifecycleOwner, observer)
            value?.observe(viewLifecycleOwner, activeAccountListObserver)
        }


    private var accountListAdapter: AccountListAdapter ? = null

    private var activeAccountAdapter: ActiveAccountAdapter ? = null

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

        mPresenter = AccountListPresenter(requireContext(),arguments.toStringMap(),this,
                accountManager = UstadAccountManager.getInstance(UstadMobileSystemImpl.instance,requireContext()))

        //version text - where do we get it
        accountListAdapter = AccountListAdapter(mPresenter)
        aboutItemAdapter = AboutItemAdapter("Version 0.2.1 'KittyHawk'",mPresenter)
        activeAccountAdapter = ActiveAccountAdapter(mPresenter)

        newItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
                createNewText = String.format(getString(R.string.add_another),
                        getString(R.string.account).toLowerCase()))
        newItemRecyclerViewAdapter?.newItemVisible = true

        mergeRecyclerAdapter = MergeAdapter(activeAccountAdapter, accountListAdapter,
                newItemRecyclerViewAdapter, aboutItemAdapter)

        mBinding?.accountListRecycler?.adapter = mergeRecyclerAdapter

        mPresenter?.onCreate(savedInstanceState.toStringMap())

        return rootView
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
        accountListAdapter = null
        activeAccountAdapter = null
        mergeRecyclerAdapter = null
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