package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.ustadmobile.libuicompose.components.UstadDateField
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
actual fun RegisterAgeRedirectDatePicker(
    date: Long,
    onSetDate: (Long) -> Unit,
    supportingText: @Composable () -> Unit,
    isError: Boolean,
    maxDate: Long,
    onDone: () -> Unit,
) {

    UstadDateField(
        modifier = Modifier.fillMaxWidth(),
        value = date,
        label = {
            Text(stringResource(MR.strings.birthday) + "*")
        },
        timeZoneId = "UTC",
        onValueChange = onSetDate,
        supportingText = supportingText,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        )
    )

}