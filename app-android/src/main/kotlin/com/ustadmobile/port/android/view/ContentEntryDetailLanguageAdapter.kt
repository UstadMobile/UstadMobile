package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage

class ContentEntryDetailLanguageAdapter(private val data: List<ContentEntryRelatedEntryJoinWithLanguage>, val listener: AdapterViewListener, private val entryUid: Long) : RecyclerView.Adapter<ContentEntryDetailLanguageAdapter.LangHolder>() {

    interface AdapterViewListener {
        fun selectContentEntryOfLanguage(contentEntryUid: Long)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LangHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_entry_lang, parent, false)
        return LangHolder(view)
    }

    override fun onBindViewHolder(holder: LangHolder, position: Int) {
        val entry = data[position]
        holder.entryLang.text = entry.languageName
        holder.entryLang.setOnClickListener { view -> listener.selectContentEntryOfLanguage(entry.cerejRelatedEntryUid) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class LangHolder(view: View) : RecyclerView.ViewHolder(view) {
        val entryLang: Button = view as AppCompatButton

        override fun toString(): String {
            return entryLang.text.toString()
        }
    }


    private object VIEW_TYPES {
        val Header = 1
        val Normal = 2
        val Footer = 3
    }

}
