package com.ustadmobile.libuicompose.view.courseterminology.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditUiState
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditViewModel
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadErrorText
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun CourseTerminologyEditScreen(
    uiState: CourseTerminologyEditUiState = CourseTerminologyEditUiState(),
    onTerminologyTermChanged: (TerminologyEntry) -> Unit = {},
    onTerminologyChanged: (CourseTerminology?) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    )  {

        item(key = "terms_title") {
            Column {
                OutlinedTextField(
                    modifier = Modifier.testTag("terms_title")
                        .defaultItemPadding()
                        .fillMaxWidth(),
                    singleLine = true,
                    value = uiState.entity?.ctTitle ?: "",
                    onValueChange = {
                        onTerminologyChanged(uiState.entity?.shallowCopy {
                            ctTitle = it
                        })
                    },
                    label = { Text(stringResource(MR.strings.name_key)) },
                    enabled = uiState.fieldsEnabled,
                )

                uiState.titleError?.also { UstadErrorText(error = it) }
            }
        }

        item(key = "your_words_for") {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(MR.strings.your_words_for)
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
                    singleLine = true,
                    label = { Text(stringResource(terminologyTerm.stringResource)) },
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
    val uiState: CourseTerminologyEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        CourseTerminologyEditUiState(), Dispatchers.Main.immediate
    )

    CourseTerminologyEditScreen(
        uiState = uiState,
        onTerminologyChanged = viewModel::onEntityChanged,
        onTerminologyTermChanged = viewModel::onTerminologyTermChanged
    )
}
