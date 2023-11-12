package com.ustadmobile.libuicompose.view

import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.appstate.AppUiState
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()

        val appUiState = remember {
            mutableStateOf(AppUiState())
        }

        val appUiStateVal by appUiState

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
                            Text(appUiStateVal.title ?: "Ustad Mobile")
                        }
                    )
                }
            ) { innerPadding ->
                AppNavHost(
                    navigator = navigator,
                    onSetAppUiState = appUiState.component2(),
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}