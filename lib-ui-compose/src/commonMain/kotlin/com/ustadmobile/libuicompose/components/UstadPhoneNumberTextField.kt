package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.libuicompose.util.phonenum.CountryCodeTransformation
import com.ustadmobile.libuicompose.util.phonenum.CountryCodeTransformation.Companion.COUNTRY_CODE_MAX_LEN
import com.ustadmobile.libuicompose.util.phonenum.PhoneNumberTransformation
import com.ustadmobile.libuicompose.util.phonenum.guessInitialPhoneCountryCode
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.math.min

/**
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadPhoneNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onNationalNumberSetChanged: ((Boolean) -> Unit)? = null,
    label: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {

    //As per https://developermemos.com/posts/checking-composable-render-preview
    // DI will not be accessible on preview mode, so we want to avoid attempting to use it there.
    val isPreview = LocalInspectionMode.current

    val di = localDI()

    val phoneUtil = remember {
        di.direct.instance<IPhoneNumberUtil>()
    }

    val parsedValue by lazy {
        try {
            //Because we always store the country code, the defaultRegion has no effect
            phoneUtil.parse(value, "US")
        }catch(e: Throwable) {
            null
        }
    }

    val guessedCountryCode = guessInitialPhoneCountryCode(phoneUtil)

    var countryCode by remember {
        mutableStateOf(parsedValue?.countryCode?.toString() ?: guessedCountryCode?.toString() ?: "")
    }

    var nationalNumberFieldVal by remember {
        mutableStateOf(TextFieldValue(parsedValue?.countryCode?.toString() ?: ""))
    }

    /**
     * Update internal state when the value has been changed to something other than the last value
     * emitted through onValueChange e.g. when the ViewModel sends a change.
     */
    LaunchedEffect(value) {
        val stateVal = "+$countryCode${nationalNumberFieldVal.text}"
        if(value != stateVal) {
            try {
                val numParsed = phoneUtil.parse(value, "US")
                countryCode = numParsed.countryCode.toString()
                nationalNumberFieldVal = TextFieldValue(numParsed.nationalNumber.toString())
                onNationalNumberSetChanged?.invoke(true)
            }catch(e: Throwable) {
                countryCode = guessedCountryCode?.toString() ?: ""
                nationalNumberFieldVal = TextFieldValue("")
                onNationalNumberSetChanged?.invoke(false)
            }
        }
    }

    val visualTransformation = remember(countryCode) {
        PhoneNumberTransformation(phoneUtil.getAsYouTypeFormatter(
            regionCode = phoneUtil.getRegionForCountryCode(countryCode.toIntOrNull() ?: 1)),
            countryPrefix = "+$countryCode",
            onGetSelectionEnd = {
                nationalNumberFieldVal.selection.end
            }
        )
    }

    val countryCodeTransformation = remember {
        CountryCodeTransformation()
    }

    var countryCodesExpanded by remember {
        mutableStateOf(false)
    }


    val allCountryCodes = remember {
        phoneUtil.getSupportedRegions()
            .map { phoneUtil.getCountryCodeForRegion(it) }
            .toSet()
            .map { it.toString() }
            .sorted()
    }

    Row(modifier = modifier) {

        OutlinedTextField(
            value = countryCode,
            modifier = Modifier.width(112.dp),
            label = {
                Text("")
            },
            singleLine = true,
            isError = isError,
            visualTransformation = countryCodeTransformation,
            onValueChange = { newText ->
                val filteredVal = newText.filter { it.isDigit() }
                val newCountryCode = if(filteredVal.isNotEmpty()) {
                    filteredVal.substring(0, min(COUNTRY_CODE_MAX_LEN, filteredVal.length))
                }else {
                    ""
                }
                countryCode = newCountryCode
                onValueChange("+$newCountryCode${nationalNumberFieldVal.text}")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        countryCodesExpanded = !countryCodesExpanded
                    }
                ) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier.rotate(if (countryCodesExpanded) 180f else 0f)
                    )
                }

                DropdownMenu(
                    expanded = countryCodesExpanded,
                    onDismissRequest = {
                        countryCodesExpanded = false
                    }
                ) {

                    allCountryCodes.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region )},
                            onClick = {
                                countryCode = region
                                onValueChange("+$region${nationalNumberFieldVal.text}")
                                countryCodesExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            },
        )

        Spacer(Modifier.width(16.dp))

        OutlinedTextField(
            value = nationalNumberFieldVal,
            onValueChange = {
                val filteredTextVal = it.text.filter { it.isDigit() }
                nationalNumberFieldVal = it.copy(
                    text = filteredTextVal
                )
                onValueChange("+$countryCode$filteredTextVal")
                onNationalNumberSetChanged?.invoke(filteredTextVal.isNotEmpty())
            },
            label = label,
            visualTransformation = visualTransformation,
            singleLine = true,
            isError = isError,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            supportingText =  supportingText,
        )
    }

}