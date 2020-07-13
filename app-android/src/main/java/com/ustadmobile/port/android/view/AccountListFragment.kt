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
import com.toughra.ustadmobile.databinding.ItemAccountActiveBinding
import com.toughra.ustadmobile.databinding.ItemAccountListBinding
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class AccountListFragment : UstadBaseFragment(), AccountListView, View.OnClickListener {

    private var mBinding: FragmentAccountListBinding? = null


    class AccountAdapter(var mPresenter: AccountListPresenter?):
            ListAdapter<UmAccount,RecyclerView.ViewHolder>(DIFF_CALLBACK_ACCOUNT),
            Observer<List<UmAccount>>{

        private var activeAccountViewHolder: ActiveAccountViewHolder ? = null

        var activeAccount: UmAccount? = null
        set(value){
            field = value
            updateAccountList()
            updateActiveAccount()
        }

        private var storedAccounts: MutableList<UmAccount>? = null

        class AccountViewHolder(val binding: ItemAccountListBinding): RecyclerView.ViewHolder(binding.root)

        class ActiveAccountViewHolder(val binding: ItemAccountActiveBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == VIEW_TYPE_ACTIVE_ACCOUNT){
                val mBinding = ItemAccountActiveBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false).apply {
                    presenter = mPresenter
                }
                return ActiveAccountViewHolder(mBinding)
            }

            val mBinding = ItemAccountListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                presenter = mPresenter
            }
            return AccountViewHolder(mBinding)
        }

        override fun getItemViewType(position: Int): Int {
            if(position == 0) return VIEW_TYPE_ACTIVE_ACCOUNT
            return VIEW_TYPE_ACCOUNT_LIST
        }

        override fun getItemCount(): Int {
            return currentList.size + 1
        }

        override fun onChanged(umAccounts: List<UmAccount>?) {
            storedAccounts = umAccounts?.toMutableList()
            updateAccountList()
        }

        private fun updateAccountList(){
            val umAccount = activeAccount
            val mStoredAccounts = storedAccounts?.toTypedArray()?.let { copyOnWriteListOf(*it) }
            if(umAccount != null){
                if(mStoredAccounts != null && mStoredAccounts.contains(umAccount)){
                    mStoredAccounts.remove(umAccount)
                }
                submitList(mStoredAccounts)
            }
        }

        private fun updateActiveAccount(){
            val mStoredAccounts = storedAccounts
            activeAccountViewHolder?.binding?.account = activeAccount
            activeAccountViewHolder?.binding?.profileBtnVisibility =
                    if(activeAccount?.personUid == 0L) View.GONE else View.VISIBLE

            activeAccountViewHolder?.binding?.logoutBtnVisibility =
                    if(mStoredAccounts != null && (mStoredAccounts.size == 1
                                    && mStoredAccounts.contains(activeAccount) || mStoredAccounts.isEmpty()))
                        View.GONE else View.VISIBLE

        }

        private fun getRealPosition(position: Int): Int{
            return if(position == 0) position else position - 1
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is AccountViewHolder){
                holder.binding.umaccount = getItem(getRealPosition(position))
            }else{
                activeAccountViewHolder = holder as ActiveAccountViewHolder
                updateActiveAccount()
            }

        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mPresenter = null
            storedAccounts = null
            activeAccount = null
        }

        companion object{

            const val VIEW_TYPE_ACTIVE_ACCOUNT = 1

            const val VIEW_TYPE_ACCOUNT_LIST = 2
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
        t -> accountAdapter?.activeAccount = t
    }

    private var accountListObserver = Observer<List<UmAccount>?> {
        loading = false
    }


    override var accountListLive: DoorLiveData<List<UmAccount>>? = null
        set(value) {
            val observer = accountAdapter ?:return
            field?.removeObserver(observer)
            field?.removeObserver(accountListObserver)
            field = value
            value?.observe(viewLifecycleOwner, observer)
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

        mPresenter = AccountListPresenter(requireContext(),arguments.toStringMap(),this, di)

        //version text - where do we get it
        accountAdapter = AccountAdapter(mPresenter)
        aboutItemAdapter = AboutItemAdapter("Version 0.2.1 'KittyHawk'",mPresenter)

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
        mBinding = null
        accountAdapter = null
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