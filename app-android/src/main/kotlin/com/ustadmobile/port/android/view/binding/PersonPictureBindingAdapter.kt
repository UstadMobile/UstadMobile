package com.ustadmobile.port.android.view.binding

import android.content.res.Resources
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File

@BindingAdapter("personPicture")
fun setPersonPicture(personPicture: ImageView, personUid: Long){

    //PICTURE : Add picture to person

    var imageLoadJob: Job? = null
    var imgPath = ""

    imageLoadJob?.cancel()

    imageLoadJob = GlobalScope.async(Dispatchers.Main) {

        val personPictureDaoRepo = UmAccountManager.getRepositoryForActiveAccount(
                personPicture.context).personPictureDao
        val personPictureDao = UmAccountManager.getActiveDatabase(
                personPicture.context).personPictureDao


        val personPictureLocal = personPictureDao.findByPersonUidAsync(personUid)?: PersonPicture()
        imgPath = personPictureDaoRepo.getAttachmentPath(personPictureLocal)?:""

        if (imgPath.isNotEmpty())
            setPictureOnView(imgPath, personPicture)
        else
            personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)

        val personPictureEntity = personPictureDaoRepo.findByPersonUidAsync(personUid)?: PersonPicture()
        imgPath = personPictureDaoRepo.getAttachmentPath(personPictureEntity)?:""

        if(personPictureLocal != personPictureEntity) {
            if (!imgPath.isEmpty())
                setPictureOnView(imgPath, personPicture)
            else
                personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)
        }
    }
}

private fun setPictureOnView(imagePath: String, theImage: ImageView) {

    val imageUri = Uri.fromFile(File(imagePath))

    Picasso
            .get()
            .load(imageUri)
            .resize(0, dpToPxImagePerson())
            .noFade()
            .into(theImage)
}

private fun dpToPxImagePerson(): Int {
    val IMAGE_PERSON_THUMBNAIL_WIDTH = 26
    return (IMAGE_PERSON_THUMBNAIL_WIDTH *
            Resources.getSystem().displayMetrics.density).toInt()
}