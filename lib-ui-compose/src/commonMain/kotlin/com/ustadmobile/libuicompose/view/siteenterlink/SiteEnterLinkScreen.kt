package com.ustadmobile.libuicompose.view.siteenterlink

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkUiState
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.components.UstadErrorText
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SiteEnterLinkScreenForViewModel(
    viewModel: SiteEnterLinkViewModel
) {
    val uiState: SiteEnterLinkUiState by viewModel.uiState.collectAsState(
        initial = SiteEnterLinkUiState()
    )


    Navigator(
        screen = SiteEnterLinkScreen(
            uiState = uiState,
            onClickNext = viewModel::onClickNext,
            onClickNewLearningEnvironment = { },
            onEditTextValueChange = viewModel::onSiteLinkUpdated,
        ),
        onBackPressed = { currentScreen ->
            true
        }
    )
}

data class SiteEnterLinkScreen(
    val uiState: SiteEnterLinkUiState = SiteEnterLinkUiState(),
    val onClickNext: () -> Unit = {},
    val onClickNewLearningEnvironment: () -> Unit = {},
    val onEditTextValueChange: (String) -> Unit = {},
) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        )  {

            Image(
                painter = painterResource(MR.images.illustration_connect),
                contentDescription = null,)

            Text(stringResource(MR.strings.please_enter_the_linK))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("site_link_text"),
                value = uiState.siteLink,
                label = { Text(stringResource(MR.strings.site_link)) },
                onValueChange = {
                    onEditTextValueChange(it)
                },
                isError = uiState.linkError != null,
                enabled = uiState.fieldsEnabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        onClickNext()
                    }
                )
            )

            uiState.linkError?.also {
                UstadErrorText(error = it)
            }


            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onClickNext,
                enabled = uiState.fieldsEnabled,
                modifier = Modifier
                    .testTag("next_button")
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary
                )
            ) {
                Text(stringResource(MR.strings.next).uppercase(),
                    color = contentColorFor(MaterialTheme.colors.secondary)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(stringResource(MR.strings.or).uppercase())

            Button(
                onClick = { navigator.push(BasicNavigationScreen(0.inc(), true)) },
                modifier = Modifier
                    .testTag("create_new_button")
                    .fillMaxWidth(),
                elevation = null,
                enabled = uiState.fieldsEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = MaterialTheme.colors.primary,
                )
            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription = "",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )

                Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                Text(stringResource(MR.strings.create_a_new_learning_env)
                    .uppercase()
                )
            }
        }
    }

}

data class BasicNavigationScreen(
    val index: Int,
    val wrapContent: Boolean = false
) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        LifecycleEffect(
            onStarted = { print("Navigator Start screen #$index") },
            onDisposed = { print("Navigator Dispose screen #$index") }
        )

        val navigator = LocalNavigator.currentOrThrow

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.run {
                if (wrapContent) {
                    padding(vertical = 16.dp).wrapContentHeight()
                } else {
                    fillMaxSize()
                }
            }
        ) {
            Text(
                text = "Screen #$index",
                style = MaterialTheme.typography.h5
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                Button(
                    enabled = navigator.canPop,
                    onClick = navigator::pop,
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(text = "Pop")
                }

                Spacer(modifier = Modifier.weight(.1f))

                Button(
                    onClick = { navigator.push(BasicNavigationScreen(index.inc(), wrapContent)) },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(text = "Push")
                }

                Spacer(modifier = Modifier.weight(.1f))

                Button(
                    onClick = { navigator.replace(BasicNavigationScreen(index.inc(), wrapContent)) },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(text = "Replace")
                }
            }

            LazyColumn(
                modifier = Modifier.height(100.dp)
            ) {
                items(100) {
                    Text("Item #$it")
                }
            }
        }
    }
}