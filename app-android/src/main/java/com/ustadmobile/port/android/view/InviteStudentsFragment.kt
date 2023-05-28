package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.InviteStudentsUiState
import com.ustadmobile.core.viewmodel.InviteStudentsViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar


class InviteStudentsFragment : UstadBaseMvvmFragment() {

    private val viewModel: InviteStudentsViewModel by ustadViewModels(::InviteStudentsViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    InviteStudentsScreenForViewModel(viewModel)
                }
            }
        }
    }
}

@Composable
private fun InviteStudentsScreenForViewModel(
    viewModel: InviteStudentsViewModel
) {
    val uiState: InviteStudentsUiState by viewModel.uiState.collectAsState(InviteStudentsUiState())
    InviteStudentsScreen(
        uiState = uiState,
        onTextFieldChanged = viewModel::onTextFieldChanged,
        onClickAddRecipient = viewModel::onClickAddRecipient,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun InviteStudentsScreen(
    uiState: InviteStudentsUiState = InviteStudentsUiState(),
    onTextFieldChanged: (String) -> Unit = {},
    onClickAddRecipient: () -> Unit = {},
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
    ) {

        items(
            items = uiState.recipients,
            key = { recipient -> recipient }
        ){ recipient ->
            Chip(
                onClick = { },
                enabled = true,
                leadingIcon = {
                    Icon(Icons.Default.Book, contentDescription = null)
                },
            ) {
                Text(recipient)
            }
        }

        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                value = uiState.textField,
                label = { Text(stringResource( R.string.name )) },
                enabled = uiState.fieldsEnabled,
                singleLine = true,
                onValueChange = {
                    onTextFieldChanged(it)
                }
            )
        }

        item {
            if (uiState.addRecipientVisible){
                TextButton(
                    onClick = {
                        onClickAddRecipient()
                    }
                ) {
                    Column {
                        Text(stringResource(R.string.add_recipient))
                        Text(uiState.textField)
                    }
                }
            }
        }

        item {
            Card {
                Row {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )
                    Text(text = stringResource(R.string.class_id))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {},
                        content = { Text(stringResource(R.string.share)) }
                    )

                    TextButton(
                        onClick = {},
                        content = { Text(stringResource(R.string.copy_link)) }
                    )
                }
            }
        }
    }

}

@Composable
@Preview
fun InviteStudentsScreenPreview() {

    val uiState = InviteStudentsUiState(
        recipients = listOf(
            "Bob Jones","Bob Jones1","Bob@gmail.com"
        ),
    )

    MdcTheme {
        InviteStudentsScreen(uiState)
    }
}