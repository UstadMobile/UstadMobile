package com.ustadmobile.port.android.view.binding

import android.graphics.drawable.Drawable
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
import com.ustadmobile.lib.db.entities.ContentEntryProgress
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.port.android.util.ext.getActivityContext

class ImageViewLifecycleObserver2(view: ImageView, registry: ActivityResultRegistry, inverseBindingListener: InverseBindingListener)
    : ViewActivityLauncherLifecycleObserver<ImageView>(view, registry, inverseBindingListener) {

    override fun onPictureTakenOrSelected(pictureUri: Uri?) {
        view.setImageFilePath(pictureUri?.toString(),null)
        inverseBindingListener?.onChange()
    }
}

@BindingAdapter(value=["imageUri", "fallbackDrawable"], requireAll = false)
fun ImageView.setImageFilePath(imageFilePath: String?, fallbackDrawable: Drawable?) {
    //start observing
    setTag(R.id.tag_imagefilepath, imageFilePath)
    val drawable = fallbackDrawable?: ContextCompat.getDrawable(context,android.R.color.transparent)
    val picasso = Picasso.get().load(if(imageFilePath != null) Uri.parse(imageFilePath) else null)
    if(drawable != null){
        picasso.placeholder(drawable).error(drawable)
    }
    picasso.noFade().into(this)

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

@BindingAdapter("attendanceTint")
fun ImageView.setAttendanceTint(attendancePercentage: Float) {
    val color = when {
        attendancePercentage > 0.8f -> R.color.successColor
        attendancePercentage > 0.6f -> R.color.secondaryColor
        else -> R.color.errorColor
    }
    setColorFilter(ContextCompat.getColor(context, color))
}

/**
 * This binder will handle situations where there is a fixed list of flags, each of which
 * corresponds to a given drawable ID
 *
 * e.g.
 *
 * class MyFragment {
 *    companion object {
 *       @JvmField
 *       val CONTENT_TYPE_ICON_MAP = mapOf(ContentEntry.TYPE_EBOOK to R.drawable.ic_book,
 *                       ContentEntry.TYPE_EXERCISE to R.drawable.ic_assignment)
 *    }
 * }
 *
 * You can then use the following in the view XML:
 *
 * &lt;import class="com.packagepath.MyFragment"/&gt;
 *
 * &lt;TextView
 * ...
 * app:imageLookupKey="@{entityObject.contentType}"
 * app:imageLookupMap="@{MyFragment.CONTENT_TYPE_ICON_MAP}"
 * /&gt;
 *
 * Optionally you can set imageLookupFallback to set an image that will be displayed in case the
 * key is not found in the map.
 */
@BindingAdapter("imageLookupKey")
fun ImageView.setImageLookupKey(imageLookupKey: Int) {
    setTag(R.id.tag_imagelookup_key, imageLookupKey)
    updateFromImageLookupMap()
}

@BindingAdapter(value=["imageLookupMap", "imageLookupFallback"], requireAll = false)
fun ImageView.setImageLookupMap(imageLookupMap: Map<Int, Int>?, imageLookupFallback: Int?) {
    setTag(R.id.tag_imagelookup_map, imageLookupMap)
    setTag(R.id.tag_imagelookup_fallback, imageLookupFallback)
    updateFromImageLookupMap()
}

@BindingAdapter(value=["iconStatusFlag"])
fun ImageView.setIconOnStatusFlag(statusFlag: Int){
    when {
        (statusFlag and ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_PASSED) == ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_PASSED -> {
            setImageResource(R.drawable.ic_baseline_check_circle_24)
        }
        (statusFlag and ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_SATISFIED) == ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_SATISFIED -> {
            setImageResource(R.drawable.ic_baseline_check_circle_24)
        }
        (statusFlag and ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_FAILED) == ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_FAILED -> {
            setImageResource(R.drawable.ic_baseline_highlight_off_24)
        }
        statusFlag == 0 -> setImageDrawable(null) 
    }
}

@Suppress("UNCHECKED_CAST")
private fun ImageView.updateFromImageLookupMap() {
    val lookupKey = getTag(R.id.tag_imagelookup_key) as? Int
    val lookupMap = getTag(R.id.tag_imagelookup_map) as? Map<Int, Int>
    val lookupFallback = getTag(R.id.tag_imagelookup_fallback) as? Int

    if(lookupKey != null && lookupMap != null) {
        val resToUse = lookupMap[lookupKey] ?: lookupFallback
        val currentImageRes = getTag(R.id.tag_imagelookup_currentres)
        if(resToUse != null && resToUse != currentImageRes) {
            setImageResource(resToUse)
            setTag(R.id.tag_imagelookup_key, resToUse)
        }
    }
}

private val ICON_ID_MAP : Map<Int, Int> by lazy {
    mapOf(CustomField.ICON_PHONE to R.drawable.ic_phone_black_24dp,
        CustomField.ICON_PERSON to R.drawable.ic_person_black_24dp,
        CustomField.ICON_CALENDAR to R.drawable.ic_event_black_24dp,
        CustomField.ICON_EMAIL to R.drawable.ic_email_black_24dp,
        CustomField.ICON_ADDRESS to R.drawable.ic_location_pin_24dp)
}

