package com.ustadmobile.port.android.view.scopedgrant.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.scopedgrant.detail.ScopedGrantDetailUiState
import com.ustadmobile.core.viewmodel.scopedgrant.detail.ScopedGrantDetailViewModel
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment



class ScopedGrantDetail2Fragment: UstadBaseMvvmFragment() {

    private val viewModel: ScopedGrantDetailViewModel by ustadViewModels(::ScopedGrantDetailViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ScopedGrantDetailScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun ScopedGrantDetailScreen(viewModel: ScopedGrantDetailViewModel){
    val uiState: ScopedGrantDetailUiState by viewModel.uiState.collectAsState(
        ScopedGrantDetailUiState()
    )

    ScopedGrantDetailScreen(
        uiState = uiState

    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantDetailScreen(
    uiState: ScopedGrantDetailUiState,
){

    LazyColumn {
        items(
            items = uiState.bitmaskList,
            key = { it.flagVal }
        ) { bitmask ->
            ListItem(
                text = {
                    Text(messageIdResource(id = bitmask.messageId))
                },
                trailing = {
                    Icon(
                        imageVector = if (bitmask.enabled) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = stringResource(
                            if(bitmask.enabled) R.string.enabled else R.string.disabled
                        )
                    )
                }
            )
        }
    }

}

@Composable
@Preview
fun ScopedGrantDetailScreenPreview(){
    ScopedGrantDetailScreen(
        uiState = ScopedGrantDetailUiState(
            bitmaskList = listOf(
                BitmaskFlag(
                    messageId = MessageID.permission_person_update,
                    flagVal = 1,
                    enabled = true
                ),
                BitmaskFlag(
                    messageId = MessageID.permission_person_insert,
                    flagVal = 2,
                    enabled = false
                )
            )
        )
    )
}