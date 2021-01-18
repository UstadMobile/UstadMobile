    package com.ustadmobile.port.android.view.binding

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import java.io.File

    /**
 *
 */
abstract class ViewActivityLauncherLifecycleObserver<V: View>(
        var view: V,
        private var registry: ActivityResultRegistry?,
        protected var inverseBindingListener: InverseBindingListener?
) : DefaultLifecycleObserver, DialogInterface.OnClickListener {

    private var cameraLauncher: ActivityResultLauncher<Uri>? = null

    private var galleryLauncher: ActivityResultLauncher<String>? = null

    private var requestCameraPermission: ActivityResultLauncher<String>? = null

    private var requestStoragePermission: ActivityResultLauncher<String>? = null

    private val cameraUriSavedStateKey: String
        get() = PREFIX_URI_KEY + view.id

    val takePictureOnClickListener = View.OnClickListener {
        takePicture()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        cameraLauncher = registry?.register("cameraFilePath_${view.id}", owner,
            ActivityResultContracts.TakePicture(), ActivityResultCallback {
                val uriStr = view.findNavController().currentBackStackEntry
                        ?.savedStateHandle?.get<String>(cameraUriSavedStateKey) ?: return@ActivityResultCallback
                onPictureTakenOrSelected(Uri.parse(uriStr))
        })

        galleryLauncher = registry?.register("galleryFilePath_${view.id}", owner,
            ActivityResultContracts.GetContent(), ActivityResultCallback {
            onPictureTakenOrSelected(it)
        })

        requestCameraPermission = registry?.register("cameraPermission_${view.id}", owner,
            ActivityResultContracts.RequestPermission(), ActivityResultCallback { granted ->
            if(granted)
                takePictureInternal()
        })

        requestStoragePermission = registry?.register("storagePermission_${view.id}", owner,
            ActivityResultContracts.RequestPermission(), ActivityResultCallback {granted ->
            if(granted) {
                openPictureInternal()
            }
        })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        cameraLauncher = null
        galleryLauncher = null
        requestCameraPermission = null
        requestStoragePermission = null
        inverseBindingListener = null
        view.setOnClickListener(null)
    }

    fun showOptionsDialog() {
        MaterialAlertDialogBuilder(view.context)
            .setTitle(R.string.change_photo)
            .setItems(OPTIONS_STRING_IDS.map { view.context.getString(it) }.toTypedArray(), this)
            .show()

    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(OPTIONS_STRING_IDS[which]) {
            R.string.remove_photo -> onPictureTakenOrSelected(null)
            R.string.take_new_photo_from_camera -> takePicture()
            R.string.select_new_photo_from_gallery -> openPicture()
        }
    }

    private fun hasPermission(permission: String): Boolean
            = ContextCompat.checkSelfPermission(view.context, permission) == PackageManager.PERMISSION_GRANTED

    fun takePicture() {
        takePictureInternal()
    }

    protected fun takePictureInternal() {
        val navController = view.findNavController()
        val fileDest = navController.createTempFileForDestination(view.context,
                "takePicture-${System.currentTimeMillis()}")
        val fileUri = FileProvider.getUriForFile(view.context.applicationContext,
                "${view.context.packageName}.provider", fileDest)

        //This is required to grant permission to the third party activity (e.g. camera) to save
        //the image. Only seems to be required on Android 4.4
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resInfoList = view.context.packageManager.queryIntentActivities(cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            view.context.grantUriPermission(packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        view.findNavController().currentBackStackEntry?.savedStateHandle?.set(
                cameraUriSavedStateKey, fileUri.toString())
        cameraLauncher?.launch(fileUri)
    }

    fun openPictureInternal() {
        galleryLauncher?.launch("image/*")
    }

    fun openPicture() {
        openPictureInternal()
    }

    abstract fun onPictureTakenOrSelected(pictureUri: Uri?)

    companion object {

        val PREFIX_URI_KEY = "galleryFileUri_"

        val OPTIONS_STRING_IDS = arrayOf(R.string.remove_photo, R.string.take_new_photo_from_camera,
                    R.string.select_new_photo_from_gallery)
    }

}