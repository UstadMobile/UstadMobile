package com.ustadmobile.libuicompose.view.adminregistration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter


@Composable
fun AddAccountSelectNewUserScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Language Dropdown
//        LanguageDropdown()

        // Logo/Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = ustadAppImagePainter(UstadImage.ILLUSTRATION_CONNECT),
                contentDescription = "Learning Tree Logo",
                modifier = Modifier.size(200.dp)
            )
        }

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* Navigate to New User Flow */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("New user")
            }
            Button(
                onClick = { /* Navigate to Existing User Flow */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Existing user")
            }
            Button(
                onClick = { /* Navigate to QR/Badge Scanner */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Scan Badge/QR code")
            }
        }
    }
}

//@Composable
//private fun LanguageDropdown(
//    uiState: ,
//    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
//) {
//    Row(
//        modifier = Modifier
//            .wrapContentHeight()
//            .fillMaxWidth()
//    ) {
//        Box {
//            UstadSetLanguageDropDown(
//                langList = uiState.languageList,
//                currentLanguage = uiState.currentLanguage,
//                onItemSelected = onSetLanguage
//            )
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//    }
//}

