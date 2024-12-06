package com.ustadmobile.libuicompose.view.adminregistration

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadPasswordField
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun NewLearningSpaceSignupScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Profile Image
        Image(
            painter = ustadAppImagePainter(UstadImage.ILLUSTRATION_CONNECT), // Placeholder image
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { /* Handle profile image click */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Username
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("admin_username"),
            value = "", // Placeholder for value, update based on state
            maxLines = 1,
            label = { Text(stringResource(MR.strings.admin_username) + "*") },
            onValueChange = { /* Handle admin username change */ },
            isError = false, // Add condition for error
            enabled = true, // Add condition for enabling/disabling
            supportingText = { Text(stringResource(MR.strings.required)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organisation Name
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("organisation_name"),
            value = "", // Placeholder for value
            maxLines = 1,
            label = { Text(stringResource(MR.strings.organisation_name) + "*") },
            onValueChange = { /* Handle organisation name change */ },
            isError = false, // Add condition for error
            enabled = true, // Add condition for enabling/disabling
            supportingText = { Text(stringResource(MR.strings.required)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Contact
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("admin_contact"),
            value = "", // Placeholder for value
            maxLines = 1,
            label = { Text(stringResource(MR.strings.admin_contact_number_email) + "*") },
            onValueChange = { /* Handle admin contact change */ },
            isError = false, // Add condition for error
            enabled = true, // Add condition for enabling/disabling
            supportingText = { Text(stringResource(MR.strings.required)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        UstadPasswordField(
            modifier = Modifier.fillMaxWidth().testTag("password"),
            value = "", // Placeholder for password value
            label = { Text(stringResource(MR.strings.password) + "*") },
            onValueChange = { /* Handle password change */ },
            isError = false, // Add condition for error
            enabled = true, // Add condition for enabling/disabling
            supportingText = { Text(stringResource(MR.strings.required)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Captcha or "I'm not a robot"
        Row(verticalAlignment = Alignment.CenterVertically) {

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = { /* Handle submit action */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = true // Add condition for enabling/disabling
        ) {
            Text(stringResource(MR.strings.submit))
        }
    }
}

