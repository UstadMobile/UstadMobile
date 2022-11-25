package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.core.viewmodel.PersonDetailViewModel
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI
import android.text.format.DateFormat
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.messageIdMapResource
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadQuickActionButton
import java.util.*

class PersonDetailFragment2 : Fragment(){

    val di: DI by closestDI()

    private val viewModel: PersonDetailViewModel by viewModels {
        provideFactory(di, this, requireArguments())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    PersonDetailScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        return
    }

    companion object {


        fun provideFactory(
            di: DI,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory = object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return PersonDetailViewModel(di, SavedStateHandleAdapter(handle)) as T
            }
        }
    }
}

@Composable
private fun PersonDetailScreen(
    uiState: PersonDetailUiState = PersonDetailUiState(),
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
    onClickManageParentalConsent: () -> Unit = {},
    onClickChat: () -> Unit = {},
    onClickClazz: (Clazz) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )  {

        Image(
            painter = painterResource(id = R.drawable.ic_person_black_24dp),
            contentDescription = null,
            modifier = Modifier
                .height(256.dp)
                .fillMaxWidth())

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

        Text(stringResource(R.string.basic_details),
            style = Typography.h4,
            modifier = Modifier.padding(8.dp))

        DetailFeilds(uiState)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(stringResource(R.string.contact_details),
            style = Typography.h4,
            modifier = Modifier.padding(8.dp))

        ContactDetails(uiState,
            onClickDial,
            onClickSms,
            onClickEmail)

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(stringResource(R.string.classes),
            style = Typography.h4,
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
                labelText = stringResource(R.string.call),
                imageId = R.drawable.ic_call_bcd4_24dp,
                onClick = onClickDial
            )

            UstadQuickActionButton(
                labelText = stringResource(R.string.text),
                imageId = R.drawable.ic_baseline_sms_24,
                onClick = onClickSms
            )
        }

        if (uiState.emailVisible){
            UstadQuickActionButton(
                labelText = stringResource(R.string.email),
                imageId = R.drawable.ic_email_black_24dp,
                onClick = onClickEmail
            )
        }

        if(uiState.showCreateAccountVisible){
            UstadQuickActionButton(
                labelText = stringResource(R.string.create_account),
                imageId = R.drawable.ic_person_black_24dp,
                onClick = onClickCreateAccount
            )
        }

        if(uiState.changePasswordVisible){
            UstadQuickActionButton(
                labelText = stringResource(R.string.change_password),
                imageId = R.drawable.person_with_key,
                onClick = onClickChangePassword
            )
        }

        if (uiState.manageParentalConsentVisible){
            UstadQuickActionButton(
                labelText = stringResource(R.string.manage_parental_consent),
                imageId = R.drawable.ic_baseline_supervised_user_circle_24,
                onClick = onClickManageParentalConsent
            )
        }

        if (uiState.chatVisible){
            UstadQuickActionButton(
                labelText = stringResource(R.string.chat),
                imageId = R.drawable.ic_baseline_chat_24,
                onClick = onClickChat
            )
        }
    }
}

@Composable
private fun DetailFeilds(uiState: PersonDetailUiState){
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp)
    ){

        val gender = messageIdMapResource(
            map = PersonConstants.GENDER_MESSAGE_ID_MAP,
            key = uiState.person?.gender ?: 1)

        val dateOfBirth = remember { DateFormat.getDateFormat(context)
            .format(Date(uiState.person?.dateOfBirth ?: 0)).toString() }

        if (uiState.dateOfBirthVisible){
            UstadDetailField(
                imageId = R.drawable.ic_date_range_black_24dp,
                valueText = dateOfBirth,
                labelText = stringResource(R.string.birthday))
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.personGenderVisible){
            UstadDetailField(
                valueText = gender,
                labelText = stringResource(R.string.gender_literal))
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.personOrgIdVisible){
            UstadDetailField(
                imageId = R.drawable.ic_badge_24dp,
                valueText = uiState.person?.personOrgId ?: "",
                labelText = stringResource(R.string.organization_id))
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.personUsernameVisible){
            UstadDetailField(
                imageId = R.drawable.ic_account_circle_black_24dp,
                valueText = uiState.person?.username ?: "",
                labelText = stringResource(R.string.username))
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
                labelText = stringResource(R.string.phone),
                imageId = R.drawable.ic_phone_black_24dp,
                onClick = onClickDial,
                secondaryActionContent = {
                    IconButton(
                        onClick = onClickSms,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Message,
                            contentDescription = stringResource(id = R.string.message),
                        )
                    }
                }
            )
        }

        if (uiState.emailVisible){
            UstadDetailField(
                imageId = R.drawable.ic_email_black_24dp,
                valueText = uiState.person?.emailAddr ?: "",
                labelText = stringResource(R.string.email),
                onClick = onClickEmail)
        }

        if (uiState.personAddressVisible){
            UstadDetailField(
                imageId = R.drawable.ic_location_pin_24dp,
                valueText = uiState.person?.personAddress ?: "",
                labelText = stringResource(R.string.address))
        }
    }
}

@Composable
private fun Classes(
    clazzes: List<Clazz> = emptyList(),
    onClickClazz: (Clazz) -> Unit = {}
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
private fun ClassItem(clazz: Clazz){
    Row(
        modifier = Modifier.padding(8.dp).
                fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painter = painterResource(id = R.drawable.ic_group_black_24dp),
            contentDescription = null,
            modifier = Modifier
                .width(35.dp))

        Text(text = clazz.clazzName ?: "")
    }
}

@Composable
private fun PersonDetailScreen(viewModel: PersonDetailViewModel) {
    val uiState: PersonDetailUiState by viewModel.uiState.collectAsState(PersonDetailUiState())
    PersonDetailScreen(uiState)
}

@Composable
@Preview
fun PersonDetailScreenPreview() {
    val uiState = PersonDetailUiState(
        person = PersonWithPersonParentJoin().apply {
            firstNames = "Bob Jones"
            phoneNum = "0799999"
            emailAddr = "Bob@gmail.com"
            gender = 2
            username = "Bob12"
            dateOfBirth = 1352958816
            personOrgId = "123"
            personAddress = "Herat"
    },
        showCreateAccountVisible = true,
        changePasswordVisible = true,
        chatVisible = true,
        clazzes = listOf(
            Clazz().apply {
                clazzName = "Jetpack Compose Class"
            },
            Clazz().apply {
                clazzName = "React Class"
            },
        )
    )
    MdcTheme{
        PersonDetailScreen(uiState)
    }
}

