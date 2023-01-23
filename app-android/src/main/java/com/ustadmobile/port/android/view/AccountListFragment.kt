package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAccountListBinding
import com.toughra.ustadmobile.databinding.ItemAccountAboutBinding
import com.toughra.ustadmobile.databinding.ItemAccountListBinding
import com.toughra.ustadmobile.databinding.ItemAccountlistIntentmessageBinding
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.viewmodel.AccountListUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadQuickActionButton
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountListItem(
    account: UserSessionWithPersonAndEndpoint?,
    trailing: @Composable (() -> Unit)? = null,
    onClickAccount: ((UserSessionWithPersonAndEndpoint) -> Unit)? = null
){
    ListItem(
        modifier = Modifier.clickable {
            if (account != null) {
                onClickAccount?.invoke(account)
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                Modifier.size(40.dp)
            )
        },
        text = {
            Text(
                text = "${account?.person?.firstNames} ${account?.person?.lastName}"
            )
        },
        secondaryText = {
            Row {
                Image(
                    painter = painterResource(id = R.drawable.person_with_key),
                    contentDescription = null,
                    Modifier.size(20.dp)
                )
                Text(
                    text = account?.person?.username ?: "",
                    modifier = Modifier
                        .padding(start = 10.dp, end = 16.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.link),
                    contentDescription = null,
                    Modifier.size(20.dp)
                )
                Text(
                    text = account?.endpoint?.url ?: "",
                    modifier = Modifier
                        .padding(start = 10.dp)
                )
            }
        },
        trailing = trailing
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountListScreen(
    uiState: AccountListUiState,
    onAccountListItemClick: (UserSessionWithPersonAndEndpoint?) -> Unit = {},
    onDeleteListItemClick: (UserSessionWithPersonAndEndpoint?) -> Unit = {},
    onAboutClick: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onMyProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ){

        item {
            AccountListItem(
                account = uiState.activeAccount
            )
        }

        item {
            Row (
                modifier = Modifier
                    .padding(start = 72.dp, bottom = 16.dp)
            ){

                OutlinedButton(
                    onClick = onMyProfileClick,
                ) {
                    Text(stringResource(R.string.my_profile).uppercase())
                }

                OutlinedButton(
                    modifier = Modifier
                        .padding(start = 10.dp),
                    onClick = onLogoutClick,
                ) {
                    Text(stringResource(R.string.logout).uppercase())
                }

            }
        }
        
        item {
            Divider(thickness = 1.dp)
        }

        items(
            uiState.accountsList,
            key = {
                it.person.personUid
            }
        ){  account ->
            AccountListItem(
                account = account,
                onClickAccount = {  selectedAccount ->
                    onAccountListItemClick(selectedAccount)
                },
                trailing = {
                    IconButton(
                        onClick = {
                            onDeleteListItemClick(account)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                        )
                    }
                },
            )
        }

        item {
            ListItem(
                modifier = Modifier
                    .clickable {
                        onAddItem()
                    },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.add)
                    )
                },
                text = {
                    Text(text = stringResource(id = R.string.add_another_account))
                }
            )
        }

        item {
            Divider(thickness = 1.dp)
        }

        item {
            ListItem(
                modifier = Modifier
                    .clickable {
                        onAboutClick()
                    },
                text = { Text(text = "About")},
                secondaryText = { Text(text = uiState.version)}
            )
        }

    }
}

@Composable
@Preview
fun AccountListScreenPreview(){
    AccountListScreen(
        uiState = AccountListUiState(
            activeAccount = UserSessionWithPersonAndEndpoint(
                userSession = UserSession().apply {
                },
                person = Person().apply {
                    firstNames = "Sara"
                    lastName = "Sarvari"
                    personUid = 9
                    username = "sara99"
                },
                endpoint = Endpoint(
                    url = "https://example.com"
                )
            ),
            accountsList = listOf(
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Ahmad"
                        lastName = "Ahmadi"
                        personUid = 4
                        username = "ahmadi"
                    },
                    endpoint = Endpoint(
                        url = "https://example.com"
                    )
                ),
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Negin"
                        lastName = "Naseri"
                        personUid = 5
                        username = "negin10"
                    },
                    endpoint = Endpoint(
                        url = "https://someweb.com"
                    )
                ),
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Ali"
                        lastName = "Asadi"
                        personUid = 6
                        username = "ali01"
                    },
                    endpoint = Endpoint(
                        url = "https://thisisalink.org"
                    )
                )
            )
        )
    )
}