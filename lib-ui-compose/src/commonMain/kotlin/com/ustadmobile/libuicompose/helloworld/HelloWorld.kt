package com.ustadmobile.libuicompose.helloworld

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import app.cash.paging.Pager
import app.cash.paging.PagingSource
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkUiState
import com.ustadmobile.libuicompose.components.HtmlClickableTextField
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import app.cash.paging.compose.*

@Composable
fun HelloWorld() {

    Navigator(
        screen = HelloWorldScreen(),
        onBackPressed = { currentScreen ->
            true
        }
    )

}

data class HelloWorldScreen(
    val uiState: SiteEnterLinkUiState = SiteEnterLinkUiState(),
    val onClickNext: () -> Unit = {},
    val onClickNewLearningEnvironment: () -> Unit = {},
    val onEditTextValueChange: (String) -> Unit = {},
) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow

        var date by remember {
            mutableStateOf((0).toLong())
        }

        Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {

            Button(
                onClick = { navigator.push(BasicNavigationScreen(true)) },
                modifier = Modifier
                    .testTag("next_screen_button")
                    .fillMaxWidth(),
                elevation = null,
                enabled = uiState.fieldsEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = MaterialTheme.colors.primary,
                )
            ) {

                Text("Next screen")

            }


            UstadDateField(
                value = date,
                label = { Text( "Date") },
                timeZoneId = "UTC",
                onValueChange = {
                    date = it
                }
            )

            var selectedOption by remember {
                mutableStateOf("Coffee")
            }

            UstadExposedDropDownMenuField(
                modifier = Modifier.fillMaxWidth(),
                value = selectedOption,
                label = "Drink",
                enabled = true,
                options = listOf("Coffee", "Tea"),
                onOptionSelected = {
                    selectedOption = it
                },
                itemText =  { it },
            )

            HtmlClickableTextField(
                html = "Complete your assignment or <b>else</b>",
                label = stringResource(MR.strings.description),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description")
            )
        }
    }

}

data class BasicNavigationScreen(
    val wrapContent: Boolean = false
) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        LifecycleEffect(
            onStarted = { print("Navigator Start screen") },
            onDisposed = { print("Navigator Dispose screen") }
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
                text = "Test Screen",
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
                    onClick = { navigator.push(BasicNavigationScreen(wrapContent)) },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(text = "Push")
                }

                Spacer(modifier = Modifier.weight(.1f))

                Button(
                    onClick = { navigator.replace(BasicNavigationScreen(wrapContent)) },
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


@Composable
fun HelloPaging() {
    lateinit var pagingSourceFactory: () -> PagingSource<Int, String>

    val pager = remember {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = pagingSourceFactory,
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(Modifier.fillMaxSize()) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.hashCode() },
            contentType = lazyPagingItems.itemContentType()
        ) { index ->
            val item = lazyPagingItems[index]
            Text(item ?: "")
        }
    }
}
