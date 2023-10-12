package com.ustadmobile.libuicompose.view.person.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailUiState
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadDetailField
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun PersonDetailScreenForViewModel(viewModel: PersonDetailViewModel) {
    val uiState: PersonDetailUiState by viewModel.uiState.collectAsState(PersonDetailUiState())
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
    uiState: PersonDetailUiState = PersonDetailUiState(),
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
//        val personPictureUri = rememberResolvedAttachmentUri(uiState.personPicture?.personPictureUri)

//        if(personPictureUri != null) {
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

        Text(stringResource(MR.strings.basic_details),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(8.dp))

        DetailFields(uiState)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(stringResource(MR.strings.contact_details),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(8.dp))

        ContactDetails(uiState,
            onClickDial,
            onClickSms,
            onClickEmail)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(stringResource(MR.strings.classes),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(8.dp))

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
//                imageId = painterResource(MR.images.ic_call_bcd4_24dp),
                onClick = onClickDial
            )

            UstadQuickActionButton(
                labelText = stringResource(MR.strings.text),
//                imageId = painterResource(MR.images.ic_baseline_sms_24),
                onClick = onClickSms
            )
        }

        if (uiState.emailVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.email),
//                imageId = painterResource(MR.images.ic_email_black_24dp),
                onClick = onClickEmail
            )
        }

        if(uiState.showCreateAccountVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.create_account),
//                imageId = painterResource(MR.images.ic_person_black_24dp),
                onClick = onClickCreateAccount
            )
        }

        if(uiState.changePasswordVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.change_password),
//                imageId = painterResource(MR.images.person_with_key),
                onClick = onClickChangePassword
            )
        }

        if (uiState.manageParentalConsentVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.manage_parental_consent),
//                imageId = painterResource(MR.images.ic_baseline_supervised_user_circle_24),
                onClick = onClickManageParentalConsent
            )
        }

        if (uiState.chatVisible){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.chat),
//                imageId = painterResource(MR.images.ic_baseline_chat_24),
                onClick = onClickChat
            )
        }
    }
}

@Composable
private fun DetailFields(uiState: PersonDetailUiState){
//    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp)
    ){

//        val gender = stringIdMapResource(
//            map = PersonConstants.GENDER_MESSAGE_ID_MAP,
//            key = uiState.person?.gender ?: 1)

//        val dateOfBirth = remember { DateFormat.getDateFormat(context)
//            .format(Date(uiState.person?.dateOfBirth ?: 0)).toString() }

        if (uiState.dateOfBirthVisible){
            UstadDetailField(
                imageId = R.drawable.ic_date_range_black_24dp,
                valueText = dateOfBirth,
                labelText = stringResource(MR.strings.birthday))
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.personGenderVisible){
            UstadDetailField(
                valueText = gender,
                labelText = stringResource(MR.strings.gender_literal),
                imageId = 0,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.personOrgIdVisible){
            UstadDetailField(
                imageId = R.drawable.ic_badge_24dp,
                valueText = uiState.person?.personOrgId ?: "",
                labelText = stringResource(MR.strings.organization_id)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.personUsernameVisible){
            UstadDetailField(
                imageId = R.drawable.ic_account_circle_black_24dp,
                valueText = uiState.person?.username ?: "",
                labelText = stringResource(MR.strings.username))
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
            UstadDetailField(
                valueText = uiState.person?.phoneNum ?: "",
                labelText = stringResource(MR.strings.phone),
                imageId = R.drawable.ic_phone_black_24dp,
                onClick = onClickDial,
                secondaryActionContent = {
                    IconButton(
                        onClick = onClickSms,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Message,
                            contentDescription = stringResource(id = CR.string.message),
                        )
                    }
                }
            )
        }

        if (uiState.emailVisible){
            UstadDetailField(
                imageId = R.drawable.ic_email_black_24dp,
                valueText = uiState.person?.emailAddr ?: "",
                labelText = stringResource(MR.strings.email),
                onClick = onClickEmail)
        }

        if (uiState.personAddressVisible){
            UstadDetailField(
                imageId = R.drawable.ic_location_pin_24dp,
                valueText = uiState.person?.personAddress ?: "",
                labelText = stringResource(MR.strings.address))
        }
    }
}

@Composable
private fun Classes(
    clazzes: List<ClazzEnrolmentWithClazzAndAttendance> = emptyList(),
    onClickClazz: (ClazzEnrolmentWithClazzAndAttendance) -> Unit = {}
){

    clazzes.forEach { clazz ->
        TextButton(
            onClick = { onClickClazz(clazz) }
        ) {
            ClassItem(clazz)
        }
    }
}

@Composable
private fun ClassItem(clazz: ClazzEnrolmentWithClazzAndAttendance){
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painter = painterResource(MR.images.ic_group_black_24dp),
            contentDescription = null,
            modifier = Modifier
                .width(35.dp))

        Text(text = clazz.clazz?.clazzName ?: "")
    }
}