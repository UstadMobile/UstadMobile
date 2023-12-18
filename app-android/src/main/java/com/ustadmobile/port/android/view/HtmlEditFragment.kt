package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.core.viewmodel.HtmlEditUiState
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.libuicompose.components.AztecEditor
import com.ustadmobile.core.R as CR

@Composable
fun HtmlEditScreen(
    uiState: HtmlEditUiState,
    onHtmlChanged: (String) -> Unit = { },
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AztecEditor(
            html = uiState.html,
            onChange = onHtmlChanged,
            modifier = Modifier.weight(1f, fill = true).fillMaxWidth()
        )



        if(uiState.wordLimit != null || uiState.charLimit != null) {
            val words = stringResource(CR.string.words)
            val characters = stringResource(CR.string.characters)
            val errorColor = MaterialTheme.colors.error
            val annotatedStr = remember(
                uiState.wordCount, uiState.wordLimit, uiState.charCount, uiState.charLimit
            ) {
                buildAnnotatedString {
                    uiState.wordLimit?.also { wordLimit ->
                        append(words)
                        append(": ")
                        val wordCountStr = "${uiState.wordCount} / $wordLimit"
                        if((uiState.wordCount ?: 0) <= wordLimit) {
                            append(wordCountStr)
                        }else {
                            withStyle(style = SpanStyle(color = errorColor)){
                                append(wordCountStr)
                            }
                        }
                        append(" ")
                    }
                    uiState.charLimit?.also { charLimit ->
                        append(characters)
                        append(": ")
                        val charCountStr = "${uiState.charCount} / $charLimit"
                        if((uiState.charCount ?: 0) <= charLimit) {
                            append(charCountStr)
                        }else {
                            withStyle(style = SpanStyle(color = errorColor)) {
                                append(charCountStr)
                            }
                        }
                    }
                }
            }

            Text(annotatedStr)
        }

    }
}

@Composable
fun HtmlEditScreen(viewModel: HtmlEditViewModel) {

    val uiState by viewModel.uiState.collectAsState(initial = HtmlEditUiState())

    HtmlEditScreen(
        uiState = uiState,
        onHtmlChanged = viewModel::onHtmlChanged
    )

}

@Preview
@Composable
fun HtmlEditScreenPreview() {
    HtmlEditScreen(
        HtmlEditUiState(
            html = "Hello World",
            wordLimit = 10,
            wordCount = 2
        )
    )
}