package com.ustadmobile.port.android.view.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.port.android.view.ContentEntryDetailLanguageAdapter

@BindingAdapter("languageAdapter")
fun RecyclerView.setTranslationAdapter(languageAdapter: ContentEntryDetailLanguageAdapter?){
    if(languageAdapter != null){
        this.adapter = languageAdapter
    }
}
