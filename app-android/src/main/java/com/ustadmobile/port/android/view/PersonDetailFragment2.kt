package com.ustadmobile.port.android.view

import android.icu.text.DateFormat.getDateInstance
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI
import android.text.format.DateFormat
import androidx.compose.material.*
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import java.util.*
import kotlin.text.Typography

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
                MaterialTheme {
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
fun PersonDetailScreen(
    uiState: PersonDetailUiState = PersonDetailUiState(),
    gender: String = "",
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
    onClickManageParentalConsent: () -> Unit = {},
    onClickChat: () -> Unit = {}
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
               .height(256.dp))

       QuickActionBar(
           uiState,
           onClickDial,
           onClickSms,
           onClickEmail,
           onClickCreateAccount,
           onClickChangePassword,
           onClickManageParentalConsent,
           onClickChat)

       Spacer(modifier = Modifier.height(10.dp))

       Divider(color = Color.LightGray, thickness = 1.dp)

       Spacer(modifier = Modifier.height(10.dp))

       Text(stringResource(R.string.basic_details))

       DetailFeilds(uiState, gender)

       Divider(color = Color.LightGray, thickness = 1.dp)

       Spacer(modifier = Modifier.height(10.dp))

       Text(stringResource(R.string.contact_details))

       ContactDetails(uiState,
           onClickDial,
           onClickSms,
           onClickEmail)

       Divider(color = Color.LightGray, thickness = 1.dp)

       Spacer(modifier = Modifier.height(10.dp))

       Text(stringResource(R.string.classes))

       Classes()
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

        if (!uiState.person?.phoneNum.isNullOrEmpty()){

            QuickActionButton(
                stringResource(R.string.call),
                R.drawable.ic_call_bcd4_24dp,
                onClickDial)

            QuickActionButton(
                stringResource(R.string.text),
                R.drawable.ic_baseline_sms_24,
                onClickSms)
        }

        if (!uiState.person?.emailAddr.isNullOrEmpty()){
            QuickActionButton(
                stringResource(R.string.email),
                R.drawable.ic_email_black_24dp,
                onClickEmail)
        }

        if(uiState.showCreateAccountVisible){
            QuickActionButton(
                stringResource(R.string.create_account),
                R.drawable.ic_person_black_24dp,
                onClickCreateAccount)
        }

        if(uiState.changePasswordVisible){
            QuickActionButton(
                stringResource(R.string.change_password),
                R.drawable.person_with_key,
                onClickChangePassword)
        }

        if (uiState.person?.parentJoin != null){
            QuickActionButton(
                stringResource(R.string.manage_parental_consent),
                R.drawable.ic_baseline_supervised_user_circle_24,
                onClickManageParentalConsent)
        }

        if (uiState.chatVisible){
            QuickActionButton(
                stringResource(R.string.chat),
                R.drawable.ic_baseline_chat_24,
                onClickChat)
        }
    }
}

@Composable
private fun DetailFeilds(uiState: PersonDetailUiState, gender: String){
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp)
    ){

        if (uiState.person?.dateOfBirth != 0L){
            DetailFeild(
                R.drawable.ic_date_range_black_24dp,
                DateFormat.getDateFormat(context)
                    .format(Date(uiState.person?.dateOfBirth ?: 0)).toString(),
                stringResource(R.string.birthday))
        }

        if (gender.isNullOrEmpty()){
            DetailFeild(0,
                gender,
                stringResource(R.string.gender_literal))
        }

        if (uiState.person.personOrgId != null){
            DetailFeild(
                R.drawable.ic_badge_24dp,
                uiState.person?.personOrgId ?: "",
                stringResource(R.string.organization_id))
        }

        if (!uiState.person?.username.isNullOrEmpty()){
            DetailFeild(
                R.drawable.ic_account_circle_black_24dp,
                uiState.person?.username ?: "",
                stringResource(R.string.username))
        }
    }
}

@Composable
private fun ContactDetails(
    uiState: PersonDetailUiState,
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},){
    Column {

        if (!uiState.person?.phoneNum.isNullOrEmpty()){
            CallRow(
                uiState.person?.phoneNum ?: "",
                onClickDial,
                onClickSms)
        }

        if (!uiState.person?.emailAddr.isNullOrEmpty()){
            DetailFeild(
                R.drawable.ic_email_black_24dp,
                uiState.person?.emailAddr ?: "",
                stringResource(R.string.email),
                onClickEmail)
        }

        if (!uiState.person?.personAddress.isNullOrEmpty()){
            DetailFeild(
                R.drawable.ic_location_pin_24dp,
                uiState.person?.personAddress ?: "",
                stringResource(R.string.address))
        }
    }
}

@Composable
private fun CallRow(
    phoneNum: String,
    onClickDial: () -> Unit = {},
    onClickSms: () -> Unit = {},){

    Row(
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        DetailFeild(
            R.drawable.ic_phone_black_24dp,
            phoneNum,
            stringResource(R.string.phone),
            onClickDial)

        TextButton(
            onClick = onClickSms
        ){
            Image(
                painter = painterResource(id = R.drawable.ic_message_bcd4_24dp),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = Color.Gray),
                modifier = Modifier
                    .size(24.dp))
        }
    }
}

@Composable
private fun Classes(){

}

@Composable
private fun DetailFeild(
    imageId: Int = 0,
    valueText: String,
    labelText: String,
    onClick: () -> Unit = {}
){
    TextButton(
        onClick = onClick
    ){
        Row {
            if (imageId != 0){
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = Color.Gray),
                    modifier = Modifier
                        .size(24.dp))
            }

            Column {
                Text(valueText,
                    style = Typography.h4,
                    color = Color.Gray)

                Text(labelText,
                    style = Typography.body2,
                    color = Color.Gray)
            }
        }
    }
}

@Composable
private fun QuickActionButton(text: String, imageId: Int, onClick: () -> Unit){
   TextButton(
       onClick = onClick
   ){
       Column{
           Image(
               painter = painterResource(id = imageId),
               contentDescription = null,
               colorFilter = ColorFilter.tint(color = colorResource(R.color.primaryColor)),
               modifier = Modifier
                   .size(24.dp))

           Text(text, color = colorResource(R.color.primaryColor))
       }
   }
}

@Composable
fun PersonDetailScreen(viewModel: PersonDetailViewModel) {
    val uiState: PersonDetailUiState by viewModel.uiState.collectAsState(PersonDetailUiState())
    val gender: String = viewModel.getGender(uiState.person?.gender ?: 0)
    PersonDetailScreen(uiState, gender)
}

@Composable
@Preview
fun PersonDetailScreenPreview() {
    val uiState = PersonDetailUiState(person = PersonWithPersonParentJoin().apply {
        firstNames = "Bob"
    }, showCreateAccountVisible = true)
    PersonDetailScreen(uiState)
}

