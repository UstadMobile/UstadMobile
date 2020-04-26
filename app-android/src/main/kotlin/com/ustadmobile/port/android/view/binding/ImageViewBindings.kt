package com.ustadmobile.port.android.view.binding

import android.net.Uri
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
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

class ImageViewLifecycleObserver2(view: ImageView, registry: ActivityResultRegistry, inverseBindingListener: InverseBindingListener)
    : ViewActivityLauncherLifecycleObserver<ImageView>(view, registry, inverseBindingListener) {

    override fun onPictureTakenOrSelected(pictureUri: Uri?) {
        view.setImageFilePath(pictureUri?.toString())
        inverseBindingListener?.onChange()
    }
}

@BindingAdapter("imageUri")
fun ImageView.setImageFilePath(imageFilePath: String?) {
    //start observing
    setTag(R.id.tag_imagefilepath, imageFilePath)
    if(imageFilePath == null){
        setImageResource(android.R.color.transparent)
        return
    }

    Picasso.get()
            .load(Uri.parse(imageFilePath))
            .noFade()
            .into(this)
}
@BindingAdapter("imageUriAttrChanged")
fun ImageView.getImageFilePath(inverseBindingListener: InverseBindingListener) {
    val activity = context.getActivityContext() as ComponentActivity
    val imageViewLifecycleObserver = ImageViewLifecycleObserver2(this,
            activity.activityResultRegistry, inverseBindingListener)
    findViewTreeLifecycleOwner()?.lifecycle?.addObserver(imageViewLifecycleObserver)

    setOnClickListener {
        imageViewLifecycleObserver.showOptionsDialog()
    }
}

@InverseBindingAdapter(attribute = "imageUri")
fun ImageView.getRealImageFilePath(): String? {
    return (getTag(R.id.tag_imagefilepath) as? String)
}
