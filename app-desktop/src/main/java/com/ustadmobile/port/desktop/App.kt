package com.ustadmobile.port.desktop

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppBarSearchUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.libuicompose.helloworld.HelloWorld
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch

//Roughly as per https://github.com/JetBrains/compose-multiplatform-desktop-template#readme
/*
 * Clicking on the run button in the IDE directly **WILL NOT WORK** - it will not find the resource
 * bundles required (probably due to the joys of Modular Java).
 *
 * Use ./gradlew app-desktop:run to run it. To debug, run the Gradle app-desktop:run task in debug
 * mode in the IDE (this can be done by selecting the Gradle task from the Gradle pane on the right
 * of Android Studio - select app-desktop -> tasks -> compose desktop -> run, then right click on run
 * and select debug.
 */

private val screens = listOf(
    "Courses",
    "Library",
    "People"
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
fun main() = application {

    val appUiState by remember { mutableStateOf(AppUiState(
        fabState = FabUiState(visible = true, icon = FabUiState.FabIcon.EDIT, text = "Edit"),
        searchState = AppBarSearchUiState()
    )) }

    val FAB_ICON_MAP = mapOf(
        FabUiState.FabIcon.ADD to Icons.Default.Add,
        FabUiState.FabIcon.EDIT to Icons.Default.Edit,
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = appUiState.title ?: "",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {


        MaterialTheme {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(appUiState.title ?: "")
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.open()
                                }},
                                contentDescription = "",
                                icon = Icons.Outlined.Menu
                            )
                        },

                        actions = {

                            IconButton(
                                onClick = {},
                                contentDescription = "",
                                icon = Icons.Outlined.KeyboardArrowLeft
                            )

                            IconButton(
                                onClick = {},
                                contentDescription = "",
                                icon = Icons.Outlined.KeyboardArrowRight
                            )

                            TextField(
                                modifier = Modifier.testTag("searchBox").fillMaxWidth(),
                                value = appUiState.searchState.searchText,
                                placeholder = {
                                    Text(text = "Search...", style = TextStyle(
                                        color = androidx.compose.ui.graphics.Color.LightGray
                                    )
                                    )
                                },
                                onValueChange = appUiState.searchState.onSearchTextChanged,
                            )

                            IconButton(
                                onClick = { },
                                contentDescription = "icn_search_clear_content_description",
                                icon = Icons.Filled.AccountCircle)

                        }
                    )
                },
                drawerShape = NavShape(400.dp, 0f),
                drawerContent = {
                    Drawer(
                        onDestinationClicked = { route ->

                        }
                    )
                },
                scaffoldState = scaffoldState,
                drawerContentColor = MaterialTheme.colors.onBackground,
                content = {
                          HelloWorld()
                },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    if (appUiState.fabState.visible){
                        ExtendedFloatingActionButton(
                            text = { appUiState.fabState.text?.let { Text(text = it) } },
                            onClick = {},
                            modifier = Modifier.padding(0.dp),
                            icon = {
                                FAB_ICON_MAP[appUiState.fabState.icon]?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null
                                    )
                                }
                            },
                            shape = CircleShape,
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: ImageVector
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

class NavShape(
    private val widthOffset: Dp,
    private val scale: Float
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                Offset.Zero,
                Offset(
                    size.width * scale + with(density) { widthOffset.toPx() },
                    size.height
                )
            )
        )
    }
}

@Composable
fun Drawer(
    onDestinationClicked: (route: String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 24.dp)
    ) {


        Image(
            painter = painterResource(MR.images.illustration_connect),
            contentDescription = null,
            modifier = Modifier.size(90.dp),
        )

        screens.forEach { screen ->
            Spacer(Modifier.height(24.dp))
            Text(
                text = screen,
                modifier = Modifier.clickable {
                    onDestinationClicked(screen)
                }
            )
        }
    }
}