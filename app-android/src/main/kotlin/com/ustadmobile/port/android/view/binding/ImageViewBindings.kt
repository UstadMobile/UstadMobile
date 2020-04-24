package com.ustadmobile.port.android.view.binding

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import java.io.File

@BindingAdapter("imageFilePath")
fun ImageView.setImageFilePath(imageFilePath: String?) {
    if(imageFilePath == null)
        return

    Picasso.get()
            .load(Uri.fromFile(File(imageFilePath)))
            .noFade()
            .into(this)
}