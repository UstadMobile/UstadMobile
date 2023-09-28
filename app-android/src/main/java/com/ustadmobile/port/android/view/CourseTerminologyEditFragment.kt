package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditUiState
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.components.UstadErrorText
import com.ustadmobile.core.MR
import com.ustadmobile.core.R as CR
import dev.icerock.moko.resources.compose.stringResource as mrStringResource
class CourseTerminologyEditFragment: UstadBaseMvvmFragment() {

    val viewModel: CourseTerminologyEditViewModel by ustadViewModels(::CourseTerminologyEditViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    CourseTerminologyEditScreen(viewModel)
                }
            }
        }

    }

}

@Composable
private fun CourseTerminologyEditScreen(
    uiState: CourseTerminologyEditUiState = CourseTerminologyEditUiState(),
    onTerminologyTermChanged: (TerminologyEntry) -> Unit = {},
    onTerminologyChanged: (CourseTerminology?) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    )  {

        item {
            Column {
                OutlinedTextField(
                    modifier = Modifier.testTag("terms_title")
                        .defaultItemPadding()
                        .fillMaxWidth(),
                    value = uiState.entity?.ctTitle ?: "",
                    onValueChange = {
                        onTerminologyChanged(uiState.entity?.shallowCopy {
                            ctTitle = it
                        })
                    },
                    label = { Text(stringResource(CR.string.name_key)) },
                    enabled = uiState.fieldsEnabled,
                )

                uiState.titleError?.also { UstadErrorText(error = it) }
            }
        }

        item {
            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = stringResource(id = CR.string.your_words_for)
            )
        }

        items(
            items = uiState.terminologyTermList,
            key = {terminologyTerm -> terminologyTerm.id}
        ){ terminologyTerm ->
            Column {
                OutlinedTextField(
                    modifier = Modifier.testTag(terminologyTerm.id)
                        .defaultItemPadding()
                        .fillMaxWidth(),
                    value = terminologyTerm.term ?: "",
                    label = { Text(mrStringResource(terminologyTerm.stringResource)) },
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onTerminologyTermChanged(terminologyTerm.copy(
                            term = it
                        ))
                    },
                )

                terminologyTerm.errorMessage?.also { UstadErrorText(error = it) }
            }
        }
    }
}

@Composable
fun CourseTerminologyEditScreen(
    viewModel: CourseTerminologyEditViewModel
) {
    val uiState: CourseTerminologyEditUiState by viewModel.uiState.collectAsState(
        CourseTerminologyEditUiState()
    )

    CourseTerminologyEditScreen(
        uiState = uiState,
        onTerminologyChanged = viewModel::onEntityChanged,
        onTerminologyTermChanged = viewModel::onTerminologyTermChanged
    )
}

@Composable
@Preview
fun CourseTerminologyEditScreenPreview() {
    val uiState = CourseTerminologyEditUiState(
        terminologyTermList = listOf(
            TerminologyEntry(
                id = "1",
                term = "First",
                stringResource = MR.strings.teacher
            ),
            TerminologyEntry(
                id = "2",
                term = "Second",
                stringResource = MR.strings.student
            ),
            TerminologyEntry(
                id = "3",
                term = "Third",
                stringResource = MR.strings.add_a_teacher
            )
        )
    )
    MdcTheme {
        CourseTerminologyEditScreen(uiState)
    }
}