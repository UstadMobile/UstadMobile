package com.ustadmobile.libuicompose.view.person.detail

//import androidx.compose.material.icons.filled.Passkey
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailUiState
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.rememberFormattedDate
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun PersonDetailScreen(viewModel: PersonDetailViewModel) {
    val uiState: PersonDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        PersonDetailUiState(), Dispatchers.Main.immediate)
    PersonDetailScreen(
        uiState = uiState,
        onClickCreateAccount = viewModel::onClickCreateAccount,
        onClickChangePassword = viewModel::onClickChangePassword,
        onClickChat = viewModel::onClickChat,
        onClickManageParentalConsent = viewModel::onClickManageParentalConsent,
        onClickClazz = viewModel::onClickClazz,
    )
}

@Composable
fun PersonDetailScreen(
    uiState: PersonDetailUiState,
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
    onClickManageParentalConsent: () -> Unit = {},
    onClickChat: () -> Unit = {},
    onClickClazz: (ClazzEnrolmentWithClazzAndAttendance) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )  {
                                 // TODO error
//        val personPictureUri = rememberResolvedAttachmentUri(uiState.personPicture?.personPictureUri)

//        if(personPictureUri != null) {
              // TODO error
//            SubcomposeAsyncImage(
//                model = personPictureUri,
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .height(256.dp)
//                    .fillMaxWidth()
//            )
//        }


        QuickActionBar(
            uiState,
            onClickDial,
            onClickSms,
            onClickEmail,
            onClickCreateAccount,
            onClickChangePassword,
            onClickManageParentalConsent,
            onClickChat)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        UstadEditHeader(stringResource(MR.strings.basic_details))

        DetailFields(uiState)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        UstadEditHeader(stringResource(MR.strings.contact_details))

        ContactDetails(uiState,
            onClickDial,
            onClickSms,
            onClickEmail)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        UstadEditHeader(stringResource(MR.strings.courses))

        Classes(uiState.clazzes, onClickClazz)
    }
}

@Composable
private fun QuickActionBar(
    uiState: PersonDetailUiState,
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
    onClickManageParentalConsent: () -> Unit = {},
    onClickChat: () -> Unit = {},
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {

        if (uiState.phoneNumVisible){

            UstadQuickActionButton(
                labelText = stringResource(MR.strings.call),
                imageVector = Icons.Filled.Call,
                onClick = onClickDial
            )

            UstadQuickActionButton(
                labelText = stringResource(MR.strings.text),
                imageVector = Icons.Filled.Sms,
                onClick = onClickSms
            )
        }

        if (uiState.emailVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.email),
                imageVector = Icons.Filled.Email,
                onClick = onClickEmail
            )
        }

        if(uiState.showCreateAccountVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.create_account),
                imageVector = Icons.Filled.Person,
                onClick = onClickCreateAccount
            )
        }

        if(uiState.changePasswordVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.change_password),
                imageVector = Icons.Default.Key,
                onClick = onClickChangePassword
            )
        }

        if (uiState.manageParentalConsentVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.manage_parental_consent),
                imageVector = Icons.Filled.SupervisedUserCircle,
                onClick = onClickManageParentalConsent
            )
        }

        if (uiState.chatVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.chat),
                imageVector = Icons.Filled.Chat,
                onClick = onClickChat
            )
        }
    }
}

@Composable
private fun DetailFields(uiState: PersonDetailUiState){

    Column(
        modifier = Modifier.padding(8.dp)
    ){

        val gender = stringIdMapResource(
            map = PersonConstants.GENDER_MESSAGE_ID_MAP,
            key = uiState.person?.gender ?: 1)

        val dateOfBirth = rememberFormattedDate(uiState.person?.dateOfBirth ?: 0, "UTC")

        if (uiState.dateOfBirthVisible){
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(dateOfBirth)},
                supportingContent = { Text(stringResource(MR.strings.birthday)) }
            )
        }

        if (uiState.personGenderVisible){
            ListItem(
                headlineContent = { Text(gender)},
                supportingContent = { Text(stringResource(MR.strings.gender_literal)) }
            )
        }

        if (uiState.personOrgIdVisible){
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.Badge,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(uiState.person?.personOrgId ?: "")},
                supportingContent = { Text(stringResource(MR.strings.organization_id)) }
            )
        }

        if (uiState.personUsernameVisible){
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(uiState.person?.username ?: "")},
                supportingContent = { Text(stringResource(MR.strings.username)) }
            )
        }
    }
}

@Composable
private fun ContactDetails(
    uiState: PersonDetailUiState,
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
){
    Column(
        modifier = Modifier.padding(8.dp)
    ) {

        if (uiState.phoneNumVisible){
            ListItem(
                modifier = Modifier.clickable(
                    onClick = onClickDial
                ),
                leadingContent = {
                    Icon(
                        Icons.Filled.Phone,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(uiState.displayPhoneNum ?: uiState.person?.phoneNum ?: "")},
                supportingContent = { Text(stringResource(MR.strings.phone)) },
                trailingContent = {
                    IconButton(
                        onClick = onClickSms,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Message,
                            contentDescription = stringResource(MR.strings.message),
                        )
                    }
                }
            )
        }

        if (uiState.emailVisible){
            ListItem(
                modifier = Modifier.clickable(
                    onClick = onClickEmail
                ),
                leadingContent = {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(uiState.person?.emailAddr ?: "")},
                supportingContent = { Text(stringResource(MR.strings.email)) }
            )
        }

        if (uiState.personAddressVisible){
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(uiState.person?.personAddress ?: "")},
                supportingContent = { Text(stringResource(MR.strings.address)) }
            )
        }
    }
}

@Composable
private fun Classes(
    clazzes: List<ClazzEnrolmentWithClazzAndAttendance> = emptyList(),
    onClickClazz: (ClazzEnrolmentWithClazzAndAttendance) -> Unit = {}
){

    clazzes.forEach { clazz ->
        ListItem(
            modifier = Modifier.clickable { onClickClazz(clazz) },
            headlineContent = {
                Text(clazz.clazz?.clazzName ?: "")
            },
            leadingContent = {
                Icon(Icons.Default.Group, contentDescription = null)
            }

        )
    }
}
