package com.ustadmobile.libuicompose.view.clazz.inviteViaContact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.OutlinedChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactViewModel
import com.ustadmobile.libuicompose.components.UstadContactPickButton
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import com.dokar.chiptextfield.m3.ChipTextFieldDefaults


@Composable
fun InviteViaContactScreen(
    viewModel: InviteViaContactViewModel
) {

    val uiState by viewModel.uiState.collectAsState(InviteViaContactUiState())
    InviteViaContactScreen(
        uiState = uiState,
        onChipSubmitClick = {
            viewModel.onClickChipSubmit(
                it
            )
        }

    )
}

class AvatarChip(text: String, val avatar: ImageVector) : Chip(text)

@Composable
fun InviteViaContactScreen(
    uiState: InviteViaContactUiState = InviteViaContactUiState(),
    onChipSubmitClick: (String) -> Unit,
) {
    val state = rememberChipTextFieldState<AvatarChip>()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(scrollState), // Make the column scrollable

    ) {


        OutlinedChipTextField(
            state = state,
            modifier = Modifier
                .weight(1f)
                .padding(10.dp).fillMaxWidth(),

            onSubmit = {

                onChipSubmitClick(it)

                null

            },

            chipStyle = ChipTextFieldDefaults.chipStyle(shape = RoundedCornerShape(20.dp)),
            textStyle = TextStyle(fontSize = 13.sp),
            chipLeadingIcon = { chip -> Avatar(chip, modifier = Modifier) },
            label = { Text(text = stringResource(MR.strings.add_username_email_phone)) },
            chipHorizontalSpacing = 8.dp,
            chipVerticalSpacing = 10.dp,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        UstadContactPickButton(
            onContactPicked = {
                if (it != null) {
                    onChipSubmitClick(it)
                }
            }
        )
    }

    LaunchedEffect(uiState.chips) {
        uiState.chips?.let { validChip ->
            validChip.forEach {
                it

                if (it.isValid) {
                    state.addChip(AvatarChip(it.text, Icons.Default.Check))
                } else {
                    state.addChip(AvatarChip(it.text, Icons.Default.Close))

                }
            }

        }
    }


}

@Composable
fun Avatar(chip: AvatarChip, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Icon(
            imageVector = chip.avatar,
            contentDescription = "Avatar",
            tint = Color.Gray,

            modifier = modifier
                .size(28.dp)
                .clip(shape = CircleShape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        )
    }

}

