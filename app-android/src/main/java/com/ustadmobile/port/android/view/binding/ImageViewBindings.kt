package com.ustadmobile.port.android.view.binding

import android.net.Uri
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.CustomField
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

@BindingAdapter("customFieldIcon")
fun ImageView.setCustomFieldIcon(customField: CustomField?) {
    val drawableId = ICON_ID_MAP[customField?.customFieldIconId ?: 0] ?: android.R.color.transparent
    setImageDrawable(ContextCompat.getDrawable(context, drawableId))
}

private val ICON_ID_MAP : Map<Int, Int> by lazy {
    mapOf(CustomField.ICON_PHONE to R.drawable.ic_phone_black_24dp,
        CustomField.ICON_PERSON to R.drawable.ic_person_black_24dp,
        CustomField.ICON_CALENDAR to R.drawable.ic_event_black_24dp,
        CustomField.ICON_EMAIL to R.drawable.ic_email_black_24dp,
        CustomField.ICON_ADDRESS to R.drawable.ic_location_pin_24dp)
}

