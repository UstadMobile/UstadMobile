package com.ustadmobile.port.android.view.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import org.kodein.di.*


@BindingAdapter("imageResIdInt")
fun ImageView.setImageResIdInt(resId: Int) {
    setImageResource(resId)
}

