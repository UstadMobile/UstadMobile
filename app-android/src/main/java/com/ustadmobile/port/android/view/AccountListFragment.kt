package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAccountListBinding
import com.toughra.ustadmobile.databinding.ItemAccountAboutBinding
import com.toughra.ustadmobile.databinding.ItemAccountListBinding
import com.toughra.ustadmobile.databinding.ItemAccountlistIntentmessageBinding
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import org.kodein.di.instance

class AccountListFragment : UstadBaseFragment(), AccountListView, View.OnClickListener {


    class AccountAdapter(var mPresenter: AccountListPresenter?, val isActiveAccount: Boolean = false):
            ListAdapter<UserSessionWithPersonAndEndpoint, AccountAdapter.AccountViewHolder>(DIFF_CALLBACK_USER_SESSION){

        class AccountViewHolder(val binding: ItemAccountListBinding): RecyclerView.ViewHolder(binding.root)


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
            val mBinding = ItemAccountListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                presenter = mPresenter

                if(!isActiveAccount) {
                    root.setOnClickListener {
                        val session = this.session
                        if(session != null)
                            mPresenter?.handleClickUserSession(session)
                    }

                    root.background  = ContextCompat.getDrawable(root.context, R.drawable.bg_listitem)
                }
            }

            return AccountViewHolder(mBinding)
        }

        override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
            val session = getItem(position)
            holder.binding.session = session
            holder.binding.activeAccount = isActiveAccount
            holder.binding.logoutBtnVisibility = if(isActiveAccount) {
                View.VISIBLE
            }else {
                View.GONE
            }
            holder.binding.profileBtnVisibility = if(isActiveAccount && holder.binding.session?.person?.username != null) {
                View.VISIBLE
            }else {
                View.GONE
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mPresenter = null
        }
    }


    class IntentMessageViewHolder(val mBinding: ItemAccountlistIntentmessageBinding): RecyclerView.ViewHolder(mBinding.root)

    class IntentMessageAdapter : ListAdapter<String, IntentMessageViewHolder>(DIFF_CALLBACK_STRING) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntentMessageViewHolder {
            return IntentMessageViewHolder(ItemAccountlistIntentmessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: IntentMessageViewHolder, position: Int) {
            holder.mBinding.message = getItem(position)
        }
    }

    private var mIntentMessageAdapter: IntentMessageAdapter? = null

    class AboutItemAdapter(private val mVersionText: String, var mPresenter: AccountListPresenter?):
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

    private var mBinding: FragmentAccountListBinding? = null

    private var mCurrentStoredAccounts: List<UserSessionWithPersonAndEndpoint>? = null

    private var mActiveAccount: UserSessionWithPersonAndEndpoint? = null

    private var activeAccountObserver = Observer<UserSessionWithPersonAndEndpoint?> {
        mActiveAccount = it
        if(it != null)
            activeAccountAdapter?.submitList(listOf(it))
    }

    private var accountListObserver = Observer<List<UserSessionWithPersonAndEndpoint>?> {
        mCurrentStoredAccounts = it
        otherAccountsAdapter?.submitList(it)
    }

    override var activeAccountLive: LiveData<UserSessionWithPersonAndEndpoint?>? = null
        set(value) {
            field?.removeObserver(activeAccountObserver)
            field = value
            value?.observe(viewLifecycleOwner, activeAccountObserver)
        }


    override var accountListLive: LiveData<List<UserSessionWithPersonAndEndpoint>>? = null
        set(value) {
            field?.removeObserver(accountListObserver)
            field = value
            value?.observe(viewLifecycleOwner, accountListObserver)
        }

    override var title: String? = null
        set(value) {
            ustadFragmentTitle = value
            field = value
        }

    override var intentMessage: String? = null
        set(value) {
            mIntentMessageAdapter?.submitList(value?.let { listOf(it) } ?: listOf())
            field = value
        }

    private var activeAccountAdapter: AccountAdapter? = null

    private var otherAccountsAdapter: AccountAdapter? = null

    private var aboutItemAdapter: AboutItemAdapter? = null

    private var mPresenter: AccountListPresenter? = null

    private var ustadListHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    private var mergeRecyclerAdapter: ConcatAdapter? = null

    override fun onClick(p0: View?) {
        mPresenter?.handleClickAddAccount()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val rootView: View
        mBinding = FragmentAccountListBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mBinding?.accountListRecycler?.layoutManager = LinearLayoutManager(requireContext())
        val impl: UstadMobileSystemImpl by instance()
        mPresenter = AccountListPresenter(requireContext(),arguments.toStringMap(),this, di,
            viewLifecycleOwner).withViewLifecycle()

        val versionText = impl.getVersion(requireContext()) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(requireContext()))
        activeAccountAdapter = AccountAdapter(mPresenter, isActiveAccount = true)
        otherAccountsAdapter = AccountAdapter(mPresenter, isActiveAccount = false)
        aboutItemAdapter = AboutItemAdapter(versionText,mPresenter)

        ustadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                createNewText = String.format(getString(R.string.add_another),
                        getString(R.string.account).lowercase()))
        ustadListHeaderRecyclerViewAdapter?.newItemVisible = true
        mIntentMessageAdapter = IntentMessageAdapter()

        mergeRecyclerAdapter = ConcatAdapter(activeAccountAdapter, mIntentMessageAdapter,
            otherAccountsAdapter, ustadListHeaderRecyclerViewAdapter, aboutItemAdapter)

        mBinding?.accountListRecycler?.adapter = mergeRecyclerAdapter

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.accountListRecycler?.adapter = null
        mBinding = null
        activeAccountAdapter = null
        aboutItemAdapter = null
        mergeRecyclerAdapter = null
        mIntentMessageAdapter = null
        mPresenter = null
    }

    companion object {

        val DIFF_CALLBACK_STRING = object: DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }

        val DIFF_CALLBACK_USER_SESSION = object: DiffUtil.ItemCallback<UserSessionWithPersonAndEndpoint>() {
            override fun areItemsTheSame(oldItem: UserSessionWithPersonAndEndpoint, newItem: UserSessionWithPersonAndEndpoint): Boolean {
                return oldItem.userSession.usUid == newItem.userSession.usUid
            }

            override fun areContentsTheSame(oldItem: UserSessionWithPersonAndEndpoint, newItem: UserSessionWithPersonAndEndpoint): Boolean {
                return oldItem.userSession.usStatus == newItem.userSession.usStatus &&
                        oldItem.person.fullName() == newItem.person.fullName() &&
                        oldItem.endpoint == newItem.endpoint
            }
        }

    }

}