package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.databinding.FragmentPersonEditBinding
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzMemberListView.Companion.ARG_HIDE_CLAZZES
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.view.PersonAccountEditFragment.Companion.USERNAME_FILTER
import com.ustadmobile.port.android.view.binding.ImageViewLifecycleObserver2
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.util.ClearErrorTextWatcher
import com.ustadmobile.port.android.view.util.RunAfterTextChangedTextWatcher
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.toughra.ustadmobile.R

interface PersonEditFragmentEventHandler {

    fun onClickNewRoleAndAssignment()
    
}

class PersonEditFragment: UstadEditFragment<PersonWithAccount>(), PersonEditView {

    private var mBinding: FragmentPersonEditBinding? = null

    private var mPresenter: PersonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    override var genderOptions: List<MessageIdOption>? = null
        set(value) {
            field = value
        }

    override var approvalPersonParentJoin: PersonParentJoin?
        get() = mBinding?.approvalPersonParentJoin
        set(value) {
            mBinding?.approvalPersonParentJoin = value
        }


    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value

            //for some reason setting the options before (and indepently from) the value causes
            // a databinding problem
            mBinding?.genderOptions = genderOptions
            mBinding?.dateTimeMode = MODE_START_OF_DAY
            mBinding?.timeZoneId = "UTC"
            loading = false
        }


    override var personPicture: PersonPicture?
        get() = mBinding?.personPicture
        set(value) {
            mBinding?.personPicture = value
        }

    override var registrationMode: Int
        get() = mBinding?.registrationMode ?: 0
        set(value) {
            mBinding?.registrationMode = value
        }

    override var parentContactError: String?
        get() = mBinding?.parentContactError
        set(value) {
            mBinding?.parentContactError = value
        }

    override var usernameError: String?
        set(value) {
            mBinding?.usernameError = value
        }
        get() = mBinding?.usernameError

    override var emailError: String?
        set(value){
            mBinding?.emailError = value
        }
        get()= mBinding?.emailError

    override var firstNamesFieldError: String?
        set(value) {
            mBinding?.firstNamesError = value
        }
        get() = mBinding?.firstNamesError

    override var lastNameFieldError: String?
        set(value) {
            mBinding?.lastNameError = value
        }
        get() = mBinding?.lastNameError

    override var genderFieldError: String?
        set(value) {
            mBinding?.genderFieldError = value
        }
        get() = mBinding?.genderFieldError

    override var passwordError: String?
        set(value) {
            mBinding?.passwordError = value
        }
        get() = mBinding?.passwordError

    override var noMatchPasswordError: String?
        set(value) {
            mBinding?.passwordConfirmError = value
            mBinding?.passwordError = value
        }
        get() = mBinding?.passwordConfirmError


    override var confirmError: String?
        set(value) {
            mBinding?.passwordConfirmError = value
        }
        get() = mBinding?.passwordConfirmError

    override var dateOfBirthError: String?
        set(value) {
            mBinding?.dateOfBirthFieldError = value
        }
        get() = mBinding?.dateOfBirthFieldError

    override var lastNameError: String?
        set(value) {
            mBinding?.lastNameError = value
        }
        get() = mBinding?.lastNameError

    override var firstNameError: String?
        set(value) {
            mBinding?.firstNamesError = value
        }
        get() = mBinding?.firstNamesError

    private var imageViewLifecycleObserver: ImageViewLifecycleObserver2? = null

    override fun navigateToNextDestination(account: UmAccount?, nextDestination: String) {
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

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewLifecycleObserver = ImageViewLifecycleObserver2(
            requireActivity().activityResultRegistry,null, 1).also {
            lifecycle.addObserver(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.imageViewLifecycleObserver = imageViewLifecycleObserver
            it.hideClazzes =  arguments?.getString(ARG_HIDE_CLAZZES)?.toBoolean() ?: false
        }

        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        mBinding?.usernameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.usernameError = null
        })

        mBinding?.firstnamesText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.firstNamesError = null
        })

        mBinding?.lastnameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.lastNameError = null
        })

        mBinding?.genderValue?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.genderFieldError = null
        })

        mBinding?.passwordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordError = null
        })

        mBinding?.confirmPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordConfirmError = null
        })

        mBinding?.birthdayText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.dateOfBirthFieldError = null
        })
        mBinding?.emailText?.addTextChangedListener(ClearErrorTextWatcher{
            mBinding?.emailError = null
        })

        mBinding?.usernameText?.filters = arrayOf(USERNAME_FILTER)
        mBinding?.parentcontactText?.addTextChangedListener(RunAfterTextChangedTextWatcher {
            parentContactError = null
        })


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)

        if(registrationMode.hasFlag(PersonEditView.REGISTER_MODE_ENABLED)) {
            ustadFragmentTitle = requireContext().getString(R.string.register)
        }else {
            setEditFragmentTitle(R.string.add_a_new_person, R.string.edit_person)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding = null
        mPresenter = null
        entity = null

    }

}

@Composable
fun PersonEditScreen(){

    val genderList: List<MessageIdOption> = listOf()
    Column(
        modifier = Modifier.fillMaxHeight()
            .fillMaxWidth().background(Color.White),
    ) {

        TitleBar()

        Spacer(modifier = Modifier.height(5.dp))

        Content(genderList)
    }
}

@Composable
private fun TitleBar(){
    Row (
        modifier = Modifier.background(colorResource(R.color.primaryColor))
            .height(60.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(){}

            Text(stringResource(R.string.edit_person),
                style = Typography.h6,
                color = Color.White)
        }

        SaveButton(){}
    }
}

@Composable
private fun BackButton(onClick: () -> Unit){
    TextButton(onClick = {onClick},
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.primaryColor),
            disabledBackgroundColor =  colorResource(R.color.primaryColor))){
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_back_white_24dp),
            contentDescription = null)
    }
}@Composable

private fun SaveButton(onClick: () -> Unit){
    TextButton(onClick = {onClick},
        colors = ButtonDefaults.buttonColors(
            backgroundColor =  colorResource(R.color.primaryColor),
            disabledBackgroundColor =  colorResource(R.color.primaryColor))
    ){
        Text(stringResource(R.string.save),
            style = Typography.h6,
            color = Color.White)
    }
}

@Composable
private fun Content(genderList: List<MessageIdOption>){
    Column(
        modifier = Modifier.padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SetUserImageButton(){}

        TextInput(stringResource(R.string.first_names), "")

        TextInput(stringResource(R.string.last_name), "")

        SetGenderMenu(
            genderList = genderList,
            selectedGender = "",
            onItemSelected = {})

        TextInput(stringResource(R.string.birthday), "")

        TextInput(stringResource(R.string.phone_number), "")

        TextInput(stringResource(R.string.email), "")

        TextInput(stringResource(R.string.address), "")
    }
}

@Composable
private fun TextInput(label: String, value: String) {
    OutlinedTextField(
        value = value,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        label = { Text(text = label) },
        onValueChange = {},
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.LightGray,
            unfocusedBorderColor = Color.Gray)
    )
}
@Composable
private fun SetUserImageButton(onClick: () -> Unit){
    Button(onClick = {onClick},
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SetGenderMenu(
    genderList: List<MessageIdOption>,
    selectedGender: String,
    onItemSelected: (MessageIdOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedGender,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            label = { stringResource(R.string.gender_literal) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = colorResource(R.color.primaryColor),
                unfocusedIndicatorColor = colorResource(R.color.primaryColor),
                disabledIndicatorColor = colorResource(R.color.primaryColor),
            )
        )

        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {
            genderList.forEachIndexed { _, gender ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        expanded = false
                        onItemSelected(gender)
                    }
                ) {
                    Text(text = gender.messageStr)
                }
            }
        }
    }

}

@Preview
@Composable
private fun PersonEditPreview() {
    UstadMobileTheme {
        PersonEditScreen()
    }
}