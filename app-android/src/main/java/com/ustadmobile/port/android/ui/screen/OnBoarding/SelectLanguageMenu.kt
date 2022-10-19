package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.ui.theme.ui.theme.primary
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectLanguageMenu(onItemSelected: () -> Unit) {

    val context = LocalContext.current
    val impl =  UstadMobileSystemImpl()
    var languageOptions = impl.getAllUiLanguagesList(context)
    var expanded by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf(context.getString(R.string.language)) }
    var selectedLanguage by remember { mutableStateOf(impl.getLocale(context)) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedLanguage,
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = primary,
                unfocusedIndicatorColor = primary,
                disabledIndicatorColor = primary,
            )
        )
        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {
            languageOptions.forEachIndexed { index: Int, selectionOption: Pair<String, String> ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onItemSelected()
                        selectedLanguage = languageOptions[index].second
                        impl.setLocale(languageOptions[index].first, context)
                        expanded = false
                    }
                ) {
                    label = context.getString(R.string.language)
//                    SetLanguage(languageOptions[index].first)
                    Text(text = selectionOption.second)
                }
            }

        }

    }

}

@Composable
private fun SetLanguage(language: String) {
    val locale = Locale(language)
    val configuration = LocalConfiguration.current
    configuration.setLocale(locale)
    val resources = LocalContext.current.resources
    resources.updateConfiguration(configuration, resources.displayMetrics)
}