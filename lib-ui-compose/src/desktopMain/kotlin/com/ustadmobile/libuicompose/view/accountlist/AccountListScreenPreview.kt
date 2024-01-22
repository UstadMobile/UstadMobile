package com.ustadmobile.libuicompose.view.accountlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.viewmodel.accountlist.AccountListUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession


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