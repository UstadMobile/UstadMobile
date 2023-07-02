package com.ustadmobile.port.android.view.scopedgrant.edit

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.scopedgrant.edit.ScopedGrantEditUiState
import com.ustadmobile.core.viewmodel.scopedgrant.edit.ScopedGrantEditViewModel
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment




class ScopedGrantEdit2Fragment: UstadBaseMvvmFragment() {

    private val viewModel: ScopedGrantEditViewModel by ustadViewModels(::ScopedGrantEditViewModel)

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
                    ScopedGrantEditScreen(viewModel)
                }
            }
        }
    }

}

@Composable
fun ScopedGrantEditScreen(viewModel: ScopedGrantEditViewModel){
    val uiState: ScopedGrantEditUiState by viewModel.uiState.collectAsState(ScopedGrantEditUiState())
    ScopedGrantEditScreen(
        uiState = uiState,
        onChangedBitmask = viewModel::onToggleBitmask
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantEditScreen(
    uiState: ScopedGrantEditUiState = ScopedGrantEditUiState(),
    onChangedBitmask: (BitmaskFlag?) -> Unit = {},
) {
    LazyColumn {
        items(
            items = uiState.bitmaskList,
            key = { it.flagVal }
        ) { bitmask ->
            ListItem(
                modifier = Modifier.toggleable(
                    role = Role.Switch,
                    value = bitmask.enabled,
                    onValueChange = {
                        onChangedBitmask(bitmask.copy(enabled = it))
                    }
                ),
                text = { Text(messageIdResource(id = bitmask.messageId)) },
                trailing =  {
                    Switch(
                        onCheckedChange = {
                           onChangedBitmask(bitmask.copy(enabled = !it))
                        },
                        checked = bitmask.enabled,
                    )
                }
            )
        }
    }
}

@Composable
@Preview
fun ScopedGrantEditScreenPreview() {
    val uiState = ScopedGrantEditUiState(
        bitmaskList = listOf(
            BitmaskFlag(
                messageId = MessageID.permission_person_insert,
                flagVal = 1,
                enabled = true,
            ),
            BitmaskFlag(
                messageId = MessageID.permission_person_update,
                flagVal = 2
            )
        )
    )

    MdcTheme {
        ScopedGrantEditScreen(uiState)
    }
}