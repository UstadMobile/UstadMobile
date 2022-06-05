package com.ustadmobile.port.android.view.binding

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.util.ext.createTempFileForDestination

/**
 * This LifecycleObserver is used to support two-way viewbinding for an image uri on PersonPicture
 */
class ImageViewLifecycleObserver2(
    private var registry: ActivityResultRegistry?,
    var inverseBindingListener: InverseBindingListener?,
    private var registryId: Int
) : DefaultLifecycleObserver, DialogInterface.OnClickListener, View.OnClickListener {

    var view: ImageView? = null
        set(value) {
            field?.setOnClickListener(null)
            value?.setOnClickListener(this)
            field = value
        }

    private var cameraLauncher: ActivityResultLauncher<Uri>? = null

    private var galleryLauncher: ActivityResultLauncher<String>? = null

    private val cameraUriSavedStateKey: String
        get() = PREFIX_URI_KEY + registryId

    override fun onClick(v: View?) {
        showOptionsDialog()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        cameraLauncher = registry?.register("cameraFilePath_${registryId}", owner,
            ActivityResultContracts.TakePicture(), ActivityResultCallback {
                val viewVal = view ?: return@ActivityResultCallback
                val uriStr = viewVal.findNavController().currentBackStackEntry
                        ?.savedStateHandle?.get<String>(cameraUriSavedStateKey) ?: return@ActivityResultCallback
                onPictureTakenOrSelected(Uri.parse(uriStr))
        })

        galleryLauncher = registry?.register("galleryFilePath_${registryId}", owner,
            ActivityResultContracts.GetContent()
        ) {
            onPictureTakenOrSelected(it)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        cameraLauncher = null
        galleryLauncher = null
        inverseBindingListener = null
        view?.setOnClickListener(null)
        view = null
        registry = null
    }

    fun showOptionsDialog() {
        val viewVal = view ?: return
        MaterialAlertDialogBuilder(viewVal.context)
            .setTitle(R.string.change_photo)
            .setItems(OPTIONS_STRING_IDS.map { viewVal.context.getString(it) }.toTypedArray(), this)
            .show()

    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(OPTIONS_STRING_IDS[which]) {
            R.string.remove_photo -> onPictureTakenOrSelected(null)
            R.string.take_new_photo_from_camera -> takePicture()
            R.string.select_new_photo_from_gallery -> openPicture()
        }
    }

    fun takePicture() {
        val viewVal = view ?: return
        val navController = viewVal.findNavController()
        val fileDest = navController.createTempFileForDestination(viewVal.context,
                "takePicture-${System.currentTimeMillis()}")
        val fileUri = FileProvider.getUriForFile(viewVal.context.applicationContext,
                "${viewVal.context.packageName}.provider", fileDest)

        //This is required to grant permission to the third party activity (e.g. camera) to save
        //the image. Only seems to be required on Android 4.4
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resInfoList = viewVal.context.packageManager.queryIntentActivities(cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            viewVal.context.grantUriPermission(packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        viewVal.findNavController().currentBackStackEntry?.savedStateHandle?.set(
                cameraUriSavedStateKey, fileUri.toString())
        cameraLauncher?.launch(fileUri)
    }

    fun openPicture() {
        galleryLauncher?.launch("image/*")
    }

    fun onPictureTakenOrSelected(pictureUri: Uri?) {
        view?.setImageFilePath(pictureUri?.toString(),null)
        inverseBindingListener?.onChange()
    }

    companion object {

        val PREFIX_URI_KEY = "galleryFileUri_"

        val OPTIONS_STRING_IDS = arrayOf(R.string.remove_photo, R.string.take_new_photo_from_camera,
                    R.string.select_new_photo_from_gallery)
    }

}