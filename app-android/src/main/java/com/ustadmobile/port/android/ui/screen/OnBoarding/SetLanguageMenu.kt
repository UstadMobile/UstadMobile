package com.ustadmobile.port.android.ui.screen.OnBoarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.port.android.ui.theme.ui.theme.primary
import com.ustadmobile.port.android.util.ext.getActivityContext
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SetLanguageMenu(
    langList: List<UstadMobileSystemCommon.UiLanguage>,
    currentLanguage: UstadMobileSystemCommon.UiLanguage,
    onItemSelected: (UstadMobileSystemCommon.UiLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = currentLanguage.langDisplay,
            onValueChange = { },
            label = { stringResource(R.string.language) },
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
            val activity = LocalContext.current.getActivityContext()
            langList.forEachIndexed { index, uiLanguage ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        expanded = false
                        onItemSelected(uiLanguage)
                        activity.recreate()
                    }
                ) {
                    Text(text = uiLanguage.langDisplay)
                }
            }
        }
    }

}
