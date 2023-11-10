package com.ustadmobile.libuicompose.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()

        var appUiState: AppUiState by remember {
            mutableStateOf(AppUiState())
        }

        MaterialTheme {
            Scaffold(
                topBar = {
                //As per https://developer.android.com/jetpack/compose/components/app-bars#small
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        title = {
                            Text(appUiState.title ?: "Ustad Mobile")
                        }
                    )
                }
            ) { innerPadding ->
                NavHost(
                    modifier = Modifier.padding(innerPadding),
                    navigator = navigator,
                    initialRoute = "/enterlink"
                ) {
                    scene(
                        route = "/${SiteEnterLinkViewModel.DEST_NAME}"
                    ) { backStackEntry ->
//                        val viewModel = viewModel(keys = listOf()) {
//
//                        }

                        Button(
                            onClick = {
                                navigator.navigate("/login?site=foo")
                            }
                        ) {
                            Text("Click")
                        }
                    }

                    scene(
                        route = "/login"
                    ) {
                        Text(it.queryString?.map?.get("site")?.firstOrNull() ?: "()")
                    }
                }
            }
        }
    }
}