package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.savedstate.SavedStateRegistryOwner
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.view.UstadView
import org.kodein.di.direct
import org.kodein.di.instance
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants.GENDER_MESSAGE_IDS
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import org.kodein.di.DI
import com.ustadmobile.core.viewmodel.PersonEditUiState
import com.ustadmobile.core.viewmodel.PersonEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.composable.UstadDateEditTextField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.lib.db.entities.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import org.kodein.di.android.x.closestDI

interface PersonEditFragmentEventHandler {

    fun onClickNewRoleAndAssignment()
    
}

class PersonEditFragment: Fragment() {

    val di: DI by closestDI()

    private val viewModel: PersonEditViewModel by viewModels {
        PersonEditFragment.provideFactory(di, this, requireArguments())
    }


    fun navigateToNextDestination(account: UmAccount?, nextDestination: String) {
        val navController = findNavController()
        val destinationProvider: DestinationProvider = di.direct.instance()

        val umNextDestination = destinationProvider.lookupDestinationName(nextDestination)
        navController.currentBackStackEntry?.savedStateHandle?.set(UstadView.ARG_SNACK_MESSAGE,
                String.format(getString(R.string.logged_in_as),account?.username,account?.endpointUrl))
        if(umNextDestination != null){
            val navOptions = NavOptions.Builder().setPopUpTo(umNextDestination.destinationId,
                    true).build()
            navController.navigate(umNextDestination.destinationId,null, navOptions)
        }
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
                    PersonEditScreen(viewModel)
                }
            }
        }
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
                return PersonEditViewModel(di, SavedStateHandleAdapter(handle)) as T
            }
        }
    }
}

@Composable
fun PersonEditScreen(
    uiState: PersonEditUiState = PersonEditUiState(),
    onPersonChanged: (PersonWithAccount?) -> Unit = {},
){
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //SetUserImageButton(onSetUserImage)

        UstadTextEditField(
            value = uiState.person?.firstNames ?: "",
            label = stringResource(id = R.string.first_names),
            error = uiState.firstNamesFieldError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy(firstNames = it))
            }
        )

        UstadTextEditField(
            value = uiState.person?.lastName ?: "",
            label = stringResource(id = R.string.last_name),
            error = uiState.lastNameError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy(lastName = it))
            }
        )

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.person?.gender ?: 0,
            label = stringResource(R.string.gender_literal),
            options = GENDER_MESSAGE_IDS,
            onOptionSelected = {
                onPersonChanged(uiState.person?.shallowCopy(gender = it.value))
            },
            error = uiState.genderFieldError,
        )

        UstadDateEditTextField(
            value = uiState.person?.dateOfBirth ?: 0,
            label = stringResource(id = R.string.birthday),
            error = uiState.dateOfBirthError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy(dateOfBirth = it))
            }
        )

        UstadTextEditField(
            value = uiState.person?.phoneNum ?: "",
            label = stringResource(id = R.string.phone_number),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy(phoneNum = it))
            }
        )

        UstadTextEditField(
            value = uiState.person?.emailAddr ?: "",
            label = stringResource(id = R.string.email),
            error = uiState.emailError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy(emailAddr = it))
            }
        )

        UstadTextEditField(
            value = uiState.person?.personAddress ?: "",
            label = stringResource(id = R.string.address),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy(personAddress = it))
            }
        )
    }
}


@Composable
private fun SetUserImageButton(onClick: () -> Unit){
    Button(onClick = onClick,
        shape = CircleShape,
        modifier = Modifier
            .size(60.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.secondaryColor),
            disabledBackgroundColor = Color.Transparent),){
        Image(
            painter = painterResource(id = R.drawable.ic_add_a_photo_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Color.White),
            modifier = Modifier
                .size(60.dp))
    }
}


@Composable
private fun PersonEditScreen(viewModel: PersonEditViewModel) {
    val uiState: PersonEditUiState by viewModel.uiState.collectAsState(PersonEditUiState())
    PersonEditScreen(
        uiState,
        onPersonChanged = {
            viewModel.onEntityChanged(it)
        }
    )
}

@Preview
@Composable
private fun PersonEditPreview() {
    val uiState = PersonEditUiState()
    PersonEditScreen(uiState)
}