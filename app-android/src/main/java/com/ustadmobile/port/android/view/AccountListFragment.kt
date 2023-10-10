package com.ustadmobile.port.android.view

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.viewmodel.accountlist.AccountListUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadAddListItem
import com.ustadmobile.core.R as CR

class AccountListFragment : UstadBaseFragment() {


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountListItem(
    account: UserSessionWithPersonAndEndpoint?,
    trailing: @Composable (() -> Unit)? = null,
    onClickAccount: ((UserSessionWithPersonAndEndpoint?) -> Unit)? = null
){
    ListItem(
        modifier = Modifier.clickable {
            onClickAccount?.invoke(account)
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
            .defaultScreenPadding()
    ){

        item {
            AccountListItem(
                account = uiState.headerAccount
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
                    Text(stringResource(CR.string.my_profile).uppercase())
                }

                OutlinedButton(
                    modifier = Modifier
                        .padding(start = 10.dp),
                    onClick = onLogoutClick,
                ) {
                    Text(stringResource(CR.string.logout).uppercase())
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
                            contentDescription = stringResource(id = CR.string.delete),
                        )
                    }
                },
            )
        }

        item {
            UstadAddListItem(
                text = stringResource(id = CR.string.add_another_account),
                onClickAdd = onAddItem,
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
            headerAccount = UserSessionWithPersonAndEndpoint(
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