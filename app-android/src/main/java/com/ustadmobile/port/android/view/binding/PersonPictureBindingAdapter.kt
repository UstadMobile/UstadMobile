package com.ustadmobile.port.android.view.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.ustadmobile.port.android.view.person.detail.PersonDetailFragment


@BindingAdapter(value=["personPicturePersonUid", "personPictureVisibilityGoneIfNoPicture"], requireAll = false)
fun ImageView.setPersonPicture(personPicturePersonUid: Long?, personPictureVisibilityGoneIfNoPicture: Boolean?){
    setImageForeignKeyAdapter(PersonDetailFragment.FOREIGNKEYADAPTER_PERSON)
    setImageForeignKey(personPicturePersonUid ?: 0L)
}

