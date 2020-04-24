package com.ustadmobile.port.android.view.binding

import android.net.Uri
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.squareup.picasso.Picasso
import java.io.File
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.util.ext.getActivityContext

//TODO: Make this show a dialog to select between removing an image, selecting an image from gallery, and taking a photo
//Left here for reference purposes for creating the dialog
//            val items = listOf(R.string.remove_photo, R.string.take_new_photo_from_camera,
//                    R.string.select_new_photo_from_gallery).map { v.context.getString(it) }.toTypedArray()
//            MaterialAlertDialogBuilder(itemView.context)
//                    .setTitle(R.string.change_photo)
//                    .setItems(items) {dialog, which ->
//                        //handle this
//                        val presenterFieldRowVal = presenterFieldRow ?: return@setItems
//                        if(which == 1) {
//                            //imageHelper?.onClickTakePicture(presenterFieldRowVal)
//                        }
//                    }
//                    .show()
class ImageViewLifecycleObserver(var imageView: ImageView?,
                                 private val registry : ActivityResultRegistry,
                                 private val inverseBindingListener: InverseBindingListener) : DefaultLifecycleObserver {

    lateinit var takePhoto: ActivityResultLauncher<Uri>

    private var imageFilePath: String? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        takePhoto = registry.register("image", owner, ActivityResultContracts.TakePicture(), ActivityResultCallback {
            imageView?.setImageFilePath(imageFilePath)
            inverseBindingListener.onChange()
        })
    }

    fun takePicture() {
        val context = imageView?.context ?: return
        val fileDest = File(context.cacheDir, "image.jpg")
        imageFilePath = fileDest.absolutePath
        val fileUri = FileProvider.getUriForFile(context.applicationContext,
                "${context.packageName}.provider", fileDest)
        takePhoto.launch(fileUri)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        imageView?.setOnClickListener(null)
        imageView = null

    }
}

@BindingAdapter("imageFilePath")
fun ImageView.setImageFilePath(imageFilePath: String?) {
    //start observing
    if(imageFilePath == null)
        return

    setTag(R.id.tag_imagefilepath, imageFilePath)
    Picasso.get()
            .load(Uri.fromFile(File(imageFilePath)))
            .noFade()
            .into(this)
}
@BindingAdapter("imageFilePathAttrChanged")
fun ImageView.getImageFilePath(inverseBindingListener: InverseBindingListener) {
    val activity = context.getActivityContext() as ComponentActivity
    val imageViewLifecycleObserver = ImageViewLifecycleObserver(this,
            activity.activityResultRegistry, inverseBindingListener)
    findViewTreeLifecycleOwner()?.lifecycle?.addObserver(imageViewLifecycleObserver)

    setOnClickListener {
        imageViewLifecycleObserver.takePicture()
    }
}

@InverseBindingAdapter(attribute = "imageFilePath")
fun ImageView.getRealImageFilePath(): String {
    return (getTag(R.id.tag_imagefilepath) as? String) ?: ""
}
