package com.ustadmobile.libuicompose.view.accountlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UserSessionWithPersonAndLearningSpace
import com.ustadmobile.core.viewmodel.accountlist.AccountListUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession


@Composable
@Preview
fun AccountListScreenPreview(){
    AccountListScreen(
        uiState = AccountListUiState(
            headerAccount = UserSessionWithPersonAndLearningSpace(
                userSession = UserSession().apply {
                },
                person = Person().apply {
                    firstNames = "Sara"
                    lastName = "Sarvari"
                    personUid = 9
                    username = "sara99"
                },
                learningSpace = LearningSpace(
                    url = "https://example.com"
                )
            ),
            accountsList = listOf(
                UserSessionWithPersonAndLearningSpace(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Ahmad"
                        lastName = "Ahmadi"
                        personUid = 4
                        username = "ahmadi"
                    },
                    learningSpace = LearningSpace(
                        url = "https://example.com"
                    )
                ),
                UserSessionWithPersonAndLearningSpace(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Negin"
                        lastName = "Naseri"
                        personUid = 5
                        username = "negin10"
                    },
                    learningSpace = LearningSpace(
                        url = "https://someweb.com"
                    )
                ),
                UserSessionWithPersonAndLearningSpace(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Ali"
                        lastName = "Asadi"
                        personUid = 6
                        username = "ali01"
                    },
                    learningSpace = LearningSpace(
                        url = "https://thisisalink.org"
                    )
                )
            )
        )
    )
}