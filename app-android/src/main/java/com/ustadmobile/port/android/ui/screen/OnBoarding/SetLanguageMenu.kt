package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.ui.theme.ui.theme.primary
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SetLanguageMenu(
    langList: List<UstadMobileSystemCommon.UiLanguage>,
    currentLanguage: UstadMobileSystemCommon.UiLanguage,
    onItemSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val impl =  UstadMobileSystemImpl(context)
    var label by remember { mutableStateOf(context.getString(R.string.language)) }
    var selectedLangDisplay by remember { mutableStateOf(currentLanguage.langDisplay) }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        selectedLangDisplay?.let {
            OutlinedTextField(
                readOnly = true,
                value = it,
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
        }
        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {
            langList.forEachIndexed { index, uiLanguage ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onItemSelected(langList[index].langCode)
                        selectedLangDisplay = langList[index].langDisplay
                        impl.setLocale(langList[index].langCode)
                        expanded = false
                        SetLanguage(context, langList[index].langCode)
                        label = context.getString(R.string.language)
                    }
                ) {
                    Text(text = uiLanguage.langDisplay)
                }
            }
        }

    }

}

private fun SetLanguage(context: Context, language: String) {
    val locale = Locale(language)
    val configuration = context.resources.configuration
    configuration.setLocale(locale)
    val resources = context.resources
    resources.updateConfiguration(configuration, resources.displayMetrics)
}