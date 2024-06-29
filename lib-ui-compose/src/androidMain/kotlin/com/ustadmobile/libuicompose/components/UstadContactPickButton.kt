package com.ustadmobile.libuicompose.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
actual fun UstadContactPickButton(
    onContactPicked: (String?) -> Unit,
    onContactError: (String?) -> Unit

) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    var dialogVisible: Boolean by remember {
        mutableStateOf(false)
    }

    /**
     * to get  contact number from contact Address
     */
    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val contactUri: Uri? = data?.data

            if (contactUri != null) {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val cursor = context.contentResolver.query(contactUri, projection, null, null, null)
                scope.launch {
                    withContext(Dispatchers.IO) {
                        cursor?.use {
                            if (it.moveToFirst()) {
                                val numberIndex =
                                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                val number = it.getString(numberIndex)

                                onContactPicked(number)
                            } else {
                                onContactError("null")

                            }
                        }
                    }
                }


            } else {
                onContactError("null")
            }
        }
    }

    /**
     * to get Email Address from contact Address
     */
    val pickContactEmailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val contactUri: Uri? = data?.data
            if (contactUri != null) {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
                val cursor = context.contentResolver.query(contactUri, projection, null, null, null)
                scope.launch {
                    withContext(Dispatchers.IO) {
                        cursor?.use {
                            if (it.moveToFirst()) {
                                val emailIndex =
                                    it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                                onContactPicked(it.getString(emailIndex))
                            } else {
                                onContactError("null")
                            }
                        }
                    }
                }

            } else {
                onContactError("null")
            }
        }
    }


    //view//

    Button(
        onClick = { dialogVisible = true },
        modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("open_content_button"),
    ) {
        Text(stringResource(MR.strings.add_from_contacts))
    }

    if (dialogVisible) {
        Dialog(
            onDismissRequest = {
                dialogVisible = false
            }
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                ListItem(
                    modifier = Modifier.clickable {
                        dialogVisible = false
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                        }
                        pickContactLauncher.launch(intent)
                    },
                    headlineContent = {
                        Text(stringResource(MR.strings.use_phone_number))
                    }
                )

                ListItem(
                    modifier = Modifier.clickable {
                        dialogVisible = false
                        val intent = Intent(
                            Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI
                        )
                        pickContactEmailLauncher.launch(intent)
                    },
                    headlineContent = {
                        Text(stringResource(MR.strings.use_email))
                    }
                )


            }
        }
    }

}