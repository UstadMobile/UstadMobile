package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.ui.theme.ui.theme.primary
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectLanguageMenu(
    langList: List<UstadMobileSystemCommon.UiLanguage>,
    currentLanguage: UstadMobileSystemCommon.UiLanguage,
    onItemSelected: (UstadMobileSystemCommon.UiLanguage) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

//    val context = LocalContext.current
//    val impl =  UstadMobileSystemImpl()
//    var languageOptions = impl.getAllUiLanguagesList(context)
//
//    var label by remember { mutableStateOf(context.getString(R.string.language)) }
//    var selectedLanguageFirst by remember { mutableStateOf(impl.getLocale(context)) }
//    var selectedLanguageSecond = languageOptions.find { it.first == selectedLanguageFirst }?.second
//
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = {
//            expanded = !expanded
//        }
//    ) {
//        selectedLanguageSecond?.let {
//            OutlinedTextField(
//                readOnly = true,
//                value = it,
//                onValueChange = { },
//                label = { Text(label) },
//                trailingIcon = {
//                    ExposedDropdownMenuDefaults.TrailingIcon(
//                        expanded = expanded
//                    )
//                },
//                colors = ExposedDropdownMenuDefaults.textFieldColors(
//                    backgroundColor = Color.White,
//                    focusedIndicatorColor = primary,
//                    unfocusedIndicatorColor = primary,
//                    disabledIndicatorColor = primary,
//                )
//            )
//        }
//        ExposedDropdownMenu(
//            modifier = Modifier.fillMaxWidth(),
//            expanded = expanded,
//            onDismissRequest = {
//                expanded = false
//
//            }
//        ) {
//            languageOptions.forEachIndexed { index: Int, selectionOption: Pair<String, String> ->
//                DropdownMenuItem(
//                    modifier = Modifier.fillMaxWidth(),
//                    onClick = {
//                        onItemSelected()
//                        selectedLanguageSecond = languageOptions[index].second
//                        impl.setLocale(languageOptions[index].first, context)
//                        expanded = false
//                        SetLanguage(context, languageOptions[index].first)
//                        label = context.getString(R.string.language)
//                    }
//                ) {
//                    Text(text = selectionOption.second)
//                }
//            }
//
//        }
//
//    }

}

private fun SetLanguage(context: Context, language: String) {
    val locale = Locale(language)
    val configuration = context.resources.configuration
    configuration.setLocale(locale)
    val resources = context.resources
    resources.updateConfiguration(configuration, resources.displayMetrics)
    val str = resources.getString(R.string.organization_id)
}