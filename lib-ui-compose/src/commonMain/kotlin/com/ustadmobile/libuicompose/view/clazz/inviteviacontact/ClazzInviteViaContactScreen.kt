package com.ustadmobile.libuicompose.view.clazz.inviteviacontact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.OutlinedChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.ClazzInviteViaContactUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.ClazzInviteViaContactViewModel
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
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.InviteViaContactChip
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn


@Composable
fun ClazzInviteViaContactScreen(
    viewModel: ClazzInviteViaContactViewModel
) {

    val uiState by viewModel.uiState.collectAsState(ClazzInviteViaContactUiState())
    ClazzInviteViaContactScreen(
        uiState = uiState,
        onChipSubmitClick = {
            viewModel.onClickChipSubmit(
                it
            )
        },
        onContactError = { viewModel.onContactError(it) },
        onChipsRemoved = { viewModel.onChipsRemoved(it) },
        onTextFieldValueChanged = {viewModel.onTextFieldValueChanged(it)},
        onValueChanged = { viewModel.onValueChanged() }

    )
}

class AvatarChip(text: String, val avatar: ImageVector) : Chip(text)

@Composable
fun ClazzInviteViaContactScreen(
    uiState: ClazzInviteViaContactUiState = ClazzInviteViaContactUiState(),
    onChipSubmitClick: (String) -> InviteViaContactChip,
    onContactError: (String) -> Unit,
    onChipsRemoved: (List<String>) -> Unit,
    onTextFieldValueChanged: (String) -> Unit,
    onValueChanged: () -> Unit,
) {
    val state = rememberChipTextFieldState<AvatarChip>()
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        OutlinedChipTextField(
            state = state,
            value = uiState.textFieldValue ?: "" ,
            onValueChange = { newValue ->
                onTextFieldValueChanged(newValue)
            },
            modifier = Modifier
                .weight(1f)
                .padding(10.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            onSubmit = {
                val inviteViaContactChip= onChipSubmitClick(it)
                onValueChanged()
                // need to return avatarChip but we are handling add and removing
                //chips from uistate , so returning the last chip from uistate if its not
                //present in uistate
                val avatarChip = AvatarChip(
                    text = inviteViaContactChip.text,
                    avatar = if (inviteViaContactChip.isValid)
                        Icons.Default.Check
                    else
                        Icons.Default.Close
                )
                if (!uiState.chips.contains(inviteViaContactChip)){
                   avatarChip
                }else{
                    null
                }

            },
            chipStyle = ChipTextFieldDefaults.chipStyle(shape = RoundedCornerShape(20.dp)),
            textStyle = MaterialTheme.typography.bodySmall,
            chipLeadingIcon = { chip -> Avatar(chip, modifier = Modifier) },
            label = { Text(text = stringResource(MR.strings.add_username_email_phone)) },
            chipHorizontalSpacing = 8.dp,
            chipVerticalSpacing = 10.dp,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            isError = uiState.contactError != null,
        )

        UstadContactPickButton(
            onContactPicked = {
                if (it != null) {
                    onChipSubmitClick(it)
                }
            },
            onContactError = {
                if (it != null) {
                    onContactError(it)
                }
            }
        )
    }

    /**
     * When a chip is added via the ViewModel uiState, then add it to the ChipTextField state
     */
    LaunchedEffect(uiState.chips) {
        uiState.chips.let { uiChips ->
            val chipsToAdd = uiChips.filterNot { chip ->
                state.chips.any { it.text == chip.text }
            }

            chipsToAdd.forEach { chip ->
                if (chip.isValid) {
                    state.addChip(AvatarChip(chip.text, Icons.Default.Check))
                } else {
                    state.addChip(AvatarChip(chip.text, Icons.Default.Close))
                }
            }
        }
    }

    /**
     * When a chip is removed by clicking the close icon, then notify the ViewModel that it has
     * been removed.
     */
    LaunchedEffect(state.chips) {
        val removedChips = state.chips.filterNot { chip ->
            uiState.chips.any { it.text == chip.text }
        }

        onChipsRemoved(removedChips.map { it.text })
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

