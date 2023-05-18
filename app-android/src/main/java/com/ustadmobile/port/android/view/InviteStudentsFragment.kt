package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.InviteStudentsUiState
import com.ustadmobile.core.viewmodel.InviteStudentsViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.ext.defaultScreenPadding


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
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun InviteStudentsScreen(
    uiState: InviteStudentsUiState = InviteStudentsUiState(),
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
    ) {

        items(
            items = uiState.studentsList,
            key = { student -> student.personUid }
        ){ student ->
            FilterChip(
                selected = true,
                onClick = { },
                enabled = true,
            ) {
                Text(student.fullName())
            }
        }
    }

}

@Composable
@Preview
fun InviteStudentsScreenPreview() {

    val uiState = InviteStudentsUiState(
        studentsList = listOf(
            Person().apply {
                personUid = 1
                firstNames = "Bob Jones"
                phoneNum = "0799999"
                emailAddr = "Bob@gmail.com"
                gender = 2
                username = "Bob12"
                dateOfBirth = 1352958816
                personOrgId = "123"
                personAddress = "Herat"
            },
            Person().apply {
                personUid = 2
                firstNames = "Bob Jones"
                phoneNum = "0799999"
                emailAddr = "Bob@gmail.com"
                gender = 2
                username = "Bob12"
                dateOfBirth = 1352958816
                personOrgId = "123"
                personAddress = "Herat"
            }
        ),
    )

    MdcTheme {
        InviteStudentsScreen(uiState)
    }
}