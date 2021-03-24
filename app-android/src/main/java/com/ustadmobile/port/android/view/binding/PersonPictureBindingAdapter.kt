package com.ustadmobile.port.android.view.binding

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import org.kodein.di.DI
import org.kodein.di.android.di
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File


@BindingAdapter(value=["personPicturePersonUid", "personPictureVisibilityGoneIfNoPicture"], requireAll = false)
@Deprecated("This is not supported")
fun ImageView.setPersonPicture(personPicturePersonUid: Long?, personPictureVisibilityGoneIfNoPicture: Boolean?){
    val personUid = personPicturePersonUid ?: 0L

    //cancel any previous image load jobs
    (getTag(R.id.tag_imageloadjob) as? Job)?.cancel()
    setTag(R.id.tag_imageloadjob, null)

    val di: DI by di(this.context)
    val accountManager: UstadAccountManager = di.direct.instance()
    val dbRepo: UmAppDatabase = di.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
    val imageLoadJob = GlobalScope.async(Dispatchers.Main) {
        val personPictureDaoRepo = dbRepo.personPictureDao
        val personPictureDao = dbRepo.personPictureDao


        val personPictureLocal = personPictureDao.findByPersonUidAsync(personUid)
        val imgPath = if(personPictureLocal != null) personPictureDaoRepo.getAttachmentPath(personPictureLocal) else null

        when {
            !imgPath.isNullOrEmpty() -> {
                if(personPictureVisibilityGoneIfNoPicture == true)
                    visibility = View.VISIBLE

                //NO_CACHE is required as the path returned by the attachment DAO remains the same
                // after data is updated
                Picasso.get()
                        .load(File(imgPath))
                        .noFade()
                        .fit()
                        .centerCrop()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(this@setPersonPicture)
            }
            personPictureVisibilityGoneIfNoPicture == true -> visibility = View.GONE
            else -> {
                setImageResource(R.drawable.ic_person_black_24dp)

                //support for Dark mode
                val typedValue = TypedValue()
                val theme: Resources.Theme = context.theme
                theme.resolveAttribute(R.attr.colorOnIconTint, typedValue, true)
                setColorFilter(typedValue.data)
            }
        }

        setTag(R.id.tag_imageloadjob, null)
    }

    setTag(R.id.tag_imageloadjob, imageLoadJob)
}

