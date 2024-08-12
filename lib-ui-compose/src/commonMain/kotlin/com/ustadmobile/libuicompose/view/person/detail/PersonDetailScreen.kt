package com.ustadmobile.libuicompose.view.person.detail

//import androidx.compose.material.icons.filled.Passkey
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailUiState
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.lib.db.composites.ClazzEnrolmentAndPersonDetailDetails
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.libuicompose.components.UstadAsyncImage
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import com.ustadmobile.libuicompose.components.UstadTransferStatusIcon
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
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
        onClickDial = viewModel::onClickDial,
        onClickSms = viewModel::onClickSms,
        onClickEmail = viewModel::onClickEmail,
        onClickPermissions = viewModel::onClickPermissions,
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
    onClickClazz: (ClazzEnrolmentAndPersonDetailDetails) -> Unit = {},
    onClickPermissions: () -> Unit = { },
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    )  {
        uiState.person?.personPicture?.personPictureUri?.also {personPictureUri ->
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.width(256.dp).height(256.dp),
                ) {
                    UstadAsyncImage(
                        uri = personPictureUri,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.clip(CircleShape).width(256.dp).height(256.dp)
                            .testTag("person_picture")
                    )

                    uiState.person?.personPictureTransferJobItem?.also {
                        UstadTransferStatusIcon(
                            transferJobItemStatus = TransferJobItemStatus.valueOf(it.tjiStatus),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Divider()
        }

        QuickActionBar(
            uiState,
            onClickDial,
            onClickSms,
            onClickEmail,
            onClickCreateAccount,
            onClickChangePassword,
            onClickManageParentalConsent,
            onClickChat,
            onClickPermissions = onClickPermissions,
        )

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
    onClickPermissions: () -> Unit = { },
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
        }

        if(uiState.sendSmsVisible) {
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

        if(uiState.showPermissionButton) {
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.permissions),
                imageVector = Icons.Default.Shield,
                onClick = onClickPermissions
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
            key = uiState.person?.person?.gender ?: 1)

        val dateOfBirth = rememberFormattedDate(
            uiState.person?.person?.dateOfBirth ?: 0, "UTC"
        )

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
                headlineContent = { Text(uiState.person?.person?.personOrgId ?: "")},
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
                headlineContent = { Text(uiState.person?.person?.username ?: "")},
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
                headlineContent = { Text(uiState.displayPhoneNum ?: uiState.person?.person?.phoneNum ?: "")},
                supportingContent = { Text(stringResource(MR.strings.phone)) },
                trailingContent = if(uiState.sendSmsVisible) {
                    {
                        IconButton(
                            onClick = onClickSms,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Message,
                                contentDescription = stringResource(MR.strings.message),
                            )
                        }
                    }
                }else {
                    null
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
                headlineContent = { Text(uiState.person?.person?.emailAddr ?: "")},
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
                headlineContent = { Text(uiState.person?.person?.personAddress ?: "")},
                supportingContent = { Text(stringResource(MR.strings.address)) }
            )
        }
    }
}

@Composable
private fun Classes(
    clazzes: List<ClazzEnrolmentAndPersonDetailDetails> = emptyList(),
    onClickClazz: (ClazzEnrolmentAndPersonDetailDetails) -> Unit = {}
){
    clazzes.forEach { clazzAndDetails ->
        ListItem(
            modifier = Modifier.clickable {
                clazzAndDetails.also(onClickClazz)
                onClickClazz(clazzAndDetails)
            },
            headlineContent = {
                Text(clazzAndDetails.clazz?.clazzName ?: "")
            },
            leadingContent = {
                Icon(Icons.Default.Group, contentDescription = null)
            }

        )
    }
}
