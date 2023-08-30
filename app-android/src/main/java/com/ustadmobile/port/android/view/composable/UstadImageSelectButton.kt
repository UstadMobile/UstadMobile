package com.ustadmobile.port.android.view.composable

import android.content.DialogInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.ustadmobile.door.ext.resolveAttachmentAndroidUri
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.port.android.impl.nav.NavHostTempFileRegistrar
import com.ustadmobile.port.android.util.compose.rememberActiveDatabase
import com.ustadmobile.port.android.util.ext.getActivityContext
import java.io.File
import com.ustadmobile.core.R as CR


private val OPTIONS_STRING_IDS = arrayOf(
    CR.string.remove_photo,
    CR.string.take_new_photo_from_camera,
    CR.string.select_new_photo_from_gallery
)

@Composable
fun UstadImageSelectButton(
    imageUri: String?,
    onImageUriChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
){
    val context = LocalContext.current

    var cameraTempPath: String? by rememberSaveable {
        mutableStateOf(null)
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { resultOk ->
            if(resultOk) {
                onImageUriChanged(cameraTempPath)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.also {
                onImageUriChanged(it.toString())
            }
        }
    )

    val listener = DialogInterface.OnClickListener { _, which ->
        when(which) {
            0 -> {
                onImageUriChanged(null)
            }
            1 -> {
                val navHostTmpFileRegistrar = context.getActivityContext() as? NavHostTempFileRegistrar
                    ?: return@OnClickListener
                val fileDest = File(context.cacheDir, "photo${systemTimeInMillis()}")
                navHostTmpFileRegistrar.registerNavDestinationTemporaryFile(fileDest)
                val fileUri = FileProvider.getUriForFile(context.applicationContext,
                    "${context.packageName}.provider", fileDest)
                cameraTempPath = fileUri.toString()
                takePhotoLauncher.launch(fileUri)
            }
            2 -> {
                galleryLauncher.launch("image/*")
            }
        }
    }


    fun showMenuDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle(CR.string.change_photo)
            .setItems(OPTIONS_STRING_IDS.map { context.getString(it) }.toTypedArray(), listener)
            .show()
    }

    if(imageUri == null) {
        Button(
            onClick = {
                showMenuDialog()
            },
            shape = CircleShape,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add_a_photo_24),
                contentDescription = stringResource(CR.string.change_photo),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSecondary),
                modifier = Modifier.size(48.dp)
            )
        }
    }else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .clickable {
                    showMenuDialog()
                },
            contentAlignment = Alignment.Center,
        ) {
            val db = rememberActiveDatabase()

            SubcomposeAsyncImage(
                model = db?.resolveAttachmentAndroidUri(imageUri) ?: imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
            )

            Spacer(
                modifier = Modifier
                    .background(Color(0,0,0, 64))
                    .fillMaxSize()
            )

            Image(
                painter = painterResource(id = R.drawable.ic_add_a_photo_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
@Preview
fun UstadImageSelectButtonPreview() {


    UstadImageSelectButton(
        imageUri = "http://localhost/vsoxapi/unzip/assets/htmlimages/Answer_radio_select.png",
        onImageUriChanged = { },
        modifier = Modifier.size(60.dp),
    )
}
