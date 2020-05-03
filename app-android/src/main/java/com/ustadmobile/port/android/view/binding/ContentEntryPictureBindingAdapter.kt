package com.ustadmobile.port.android.view.binding

import android.content.res.Resources
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UmAccountManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

@BindingAdapter("contentPicture")
fun setContentPicture(contentPicture: ImageView, contentEntryUid: Long){


    var imageLoadJob: Job? = null
    var imgUrl = ""

    imageLoadJob?.cancel()

    imageLoadJob = GlobalScope.async(Dispatchers.Main) {

        val contentEntryDao = UmAccountManager.getActiveDatabase(
                contentPicture.context).contentEntryDao

        val contentEntry = contentEntryDao.findByUidAsync(contentEntryUid)
        imgUrl = contentEntry?.thumbnailUrl?:""
        if (imgUrl.isNotEmpty())
            setPictureOnView(imgUrl, contentPicture)
        else
            contentPicture.setImageResource(R.drawable.ic_collections_bookmark_black_24dp)
    }
}

private fun setPictureOnView(imageUrl: String, theImage: ImageView) {

    Picasso
            .get()
            .load(imageUrl)
            .resize(0, dpToPxImagePerson())
            .noFade()
            .into(theImage)
}

private fun dpToPxImagePerson(): Int {
    val IMAGE_PERSON_THUMBNAIL_WIDTH = 26
    return (IMAGE_PERSON_THUMBNAIL_WIDTH *
            Resources.getSystem().displayMetrics.density).toInt()
}