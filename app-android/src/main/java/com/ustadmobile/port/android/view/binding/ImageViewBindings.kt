package com.ustadmobile.port.android.view.binding

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.isContentComplete
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.door.ext.resolveAttachmentAndroidUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import kotlinx.coroutines.*
import org.kodein.di.*

@BindingAdapter(value=["imageUri", "fallbackDrawable"], requireAll = false)
fun ImageView.setImageFilePath(imageFilePath: String?, fallbackDrawable: Drawable?) {
    setTag(R.id.tag_imagefilepath, imageFilePath)
    val di: DI = (context.applicationContext as DIAware).di
    val accountManager: UstadAccountManager = di.direct.instance()
    val repo: UmAppDatabase = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
    val uriResolved = imageFilePath?.let { repo.resolveAttachmentAndroidUri(it) }

    val drawable = fallbackDrawable?: ContextCompat.getDrawable(context,android.R.color.transparent)
    val picasso = Picasso.get().load(uriResolved)
    if(drawable != null){
        picasso.placeholder(drawable).error(drawable)
    }
    picasso.noFade().into(this)

}

@BindingAdapter("imageUriAttrChanged")
fun ImageView.getImageFilePath(inverseBindingListener: InverseBindingListener) {
    setTag(R.id.tag_imageview_inversebindinglistener, inverseBindingListener)
    updateImageViewLifecycleObserver()
}

@BindingAdapter("imageViewLifecycleObserver")
fun ImageView.setImageViewLifecycleObserver(imageViewLifecycleObserver: ImageViewLifecycleObserver2) {
    setTag(R.id.tag_imageviewlifecycleobserver, imageViewLifecycleObserver)
    updateImageViewLifecycleObserver()
}

private fun ImageView.updateImageViewLifecycleObserver() {
    val lifecycleObserver = getTag(R.id.tag_imageviewlifecycleobserver) as? ImageViewLifecycleObserver2
    val inverseBindingListener = getTag(R.id.tag_imageview_inversebindinglistener) as? InverseBindingListener
    if(lifecycleObserver != null && inverseBindingListener != null) {
        lifecycleObserver.view = this
        lifecycleObserver.inverseBindingListener = inverseBindingListener
    }
}

@InverseBindingAdapter(attribute = "imageUri")
fun ImageView.getRealImageFilePath(): String? {
    return (getTag(R.id.tag_imagefilepath) as? String)
}


@BindingAdapter("imageForeignKey", "imageForeignKeyEndpoint", requireAll = false)
fun ImageView.setImageForeignKey(imageForeignKey: Long, imageForeignKeyEndpoint: String? = null){
    foreignKeyProps.foreignKey = imageForeignKey
    foreignKeyProps.foreignKeyEndpoint = imageForeignKeyEndpoint
    updateImageFromForeignKey()
}

@BindingAdapter("imageForeignKeyPlaceholder")
fun ImageView.imageForeignKeyPlaceholder(imageForeignKeyPlaceholder: Drawable?) {
    foreignKeyProps.placeholder = imageForeignKeyPlaceholder
    updateImageFromForeignKey()
}

@BindingAdapter("imageForeignKeyAutoHide")
fun ImageView.setImageForeignKeyAutoHide(autoHide: Boolean) {
    foreignKeyProps.autoHide = autoHide
}

val ImageView.foreignKeyProps: ImageViewForeignKeyProps
    get(){
        val currentProps = getTag(R.id.tag_imageforeignkey_props) as ImageViewForeignKeyProps?
        if(currentProps != null)
            return currentProps

        val newProps = ImageViewForeignKeyProps()
        setTag(R.id.tag_imageforeignkey_props, newProps)
        return newProps
    }

@BindingAdapter("imageForeignKeyAdapter")
fun ImageView.setImageForeignKeyAdapter(foreignKeyAttachmentUriAdapter: ForeignKeyAttachmentUriAdapter) {
    foreignKeyProps.foreignKeyAttachmentUriAdapter = foreignKeyAttachmentUriAdapter
    updateImageFromForeignKey()
}

/**
 * Because using a single binding adapter does not seem to trigger changes as expected when only
 * one property has changed.
 */
private fun ImageView.updateImageFromForeignKey() {
    val foreignKeyPropsVal = foreignKeyProps
    val tint = ImageViewCompat.getImageTintList(this)
    val adapter = foreignKeyPropsVal.foreignKeyAttachmentUriAdapter
    if(adapter != null && foreignKeyPropsVal.foreignKeyLoadingOrDisplayed != foreignKeyPropsVal.foreignKey) {
        //something new to load - cancel anything loading now and load this instead
        foreignKeyPropsVal.currentJob?.cancel()

        val di = (context.applicationContext as DIAware).di
        val foreignKeyVal = foreignKeyPropsVal.foreignKey
        foreignKeyPropsVal.foreignKeyLoadingOrDisplayed = foreignKeyVal

        foreignKeyPropsVal.currentJob = GlobalScope.async {
            val accountManager : UstadAccountManager = di.direct.instance()
            val endpointUrl = foreignKeyPropsVal.foreignKeyEndpoint ?: accountManager.activeAccount.endpointUrl
            val repo : UmAppDatabase = di.direct.on(Endpoint(endpointUrl)).instance(DoorTag.TAG_REPO)
            repo.onDbThenRepoWithTimeout(10000) { dbToUse: UmAppDatabase, lastResult: Uri? ->
                val uri = withTimeoutOrNull(10000) {
                    adapter.getAttachmentUri(foreignKeyVal, dbToUse)?.let {
                        repo.resolveAttachmentAndroidUri(it)
                    }
                } ?: lastResult

                withContext(Dispatchers.Main) {
                    val placeholderVal = foreignKeyPropsVal.placeholder
                    if(uri != null && uri != foreignKeyPropsVal.imageUriDisplayed ) {
                        Picasso.get().load(uri)
                            .into(this@updateImageFromForeignKey)

                        foreignKeyPropsVal.imageUriDisplayed = uri
                    }else if(uri == null && placeholderVal != null) {
                        //show placeholder
                        setImageDrawable(placeholderVal)
                        imageTintList = tint
                        foreignKeyPropsVal.imageUriDisplayed = null
                    }

                    if(foreignKeyPropsVal.autoHide) {
                        this@updateImageFromForeignKey.visibility = if(uri != null) {
                            View.VISIBLE
                        }else {
                            View.GONE
                        }
                    }
                }

                uri
            }

            //mission complete - unset job reference
            withContext(Dispatchers.Main) {
                foreignKeyPropsVal.currentJob = null
            }
        }
    }
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

@BindingAdapter(value = ["iconProgressFlag"])
fun ImageView.setIconOnProgressFlag(progress: ContentEntryStatementScoreProgress?) {
    when (progress?.isContentComplete()) {
        StatementEntity.CONTENT_COMPLETE, StatementEntity.CONTENT_PASSED -> {
            setImageResource(R.drawable.ic_content_complete)
            visibility = View.VISIBLE
        }
        StatementEntity.CONTENT_FAILED -> {
            setImageResource(R.drawable.ic_content_fail)
            visibility = View.VISIBLE
        }
        StatementEntity.CONTENT_INCOMPLETE -> {
            setImageDrawable(null)
            visibility = View.GONE
        }
        else -> {
            setImageDrawable(null)
            visibility = View.GONE
        }
    }
}

@BindingAdapter(value = ["scopedGrantEnabledIcon"])
fun ImageView.setScopedGrantEnabledIcon(enabled: Boolean) {
    setImageResource(if(enabled) R.drawable.ic_done_white_24dp else R.drawable.ic_close_black_24dp)
    contentDescription = context.getString(if(enabled) R.string.enabled else R.string.disabled)
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
        }else{
            setImageDrawable(null)
        }
    }else{
        setImageDrawable(null)
    }
}

@BindingAdapter("isContentCompleteImage")
fun ImageView.isContentCompleteImage(person: PersonWithSessionsDisplay){
    if(person.resultComplete){
        when(person.resultSuccess){
            StatementEntity.RESULT_SUCCESS -> {
                setImageResource(R.drawable.exo_ic_check)
                visibility = View.VISIBLE
            }
            StatementEntity.RESULT_FAILURE -> {
                setImageResource(R.drawable.ic_close_black_24dp)
                visibility = View.VISIBLE
            }
            StatementEntity.RESULT_UNSET ->{
                setImageDrawable(null)
                visibility = View.INVISIBLE
            }
        }
    }else{
        setImageDrawable(null)
        context.getString(R.string.incomplete)
        visibility = View.INVISIBLE
    }
}

private val ICON_ID_MAP : Map<Int, Int> by lazy {
    mapOf(CustomField.ICON_PHONE to R.drawable.ic_phone_black_24dp,
        CustomField.ICON_PERSON to R.drawable.ic_person_black_24dp,
        CustomField.ICON_CALENDAR to R.drawable.ic_event_black_24dp,
        CustomField.ICON_EMAIL to R.drawable.ic_email_black_24dp,
        CustomField.ICON_ADDRESS to R.drawable.ic_location_pin_24dp)
}

@BindingAdapter("imageResIdInt")
fun ImageView.setImageResIdInt(resId: Int) {
    setImageResource(resId)
}

@BindingAdapter("messageIconVisibility")
fun View.setMessageIconVisibility(message: MessageWithPerson){
    visibility = if(message.messageTableId == DiscussionPost.TABLE_ID){
        View.VISIBLE
    }else{
        View.GONE
    }
}

