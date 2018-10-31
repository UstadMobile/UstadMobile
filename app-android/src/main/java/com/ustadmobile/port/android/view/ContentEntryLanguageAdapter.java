package com.ustadmobile.port.android.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

public class ContentEntryLanguageAdapter extends RecyclerView.Adapter<ContentEntryLanguageAdapter.LangHolder> {


    private final List<ContentEntry> data;
    private AdapterViewListener listener;

    public ContentEntryLanguageAdapter(List<ContentEntry> data, AdapterViewListener listener){
        this.data = data;
        this.listener = listener;
    }

    protected interface AdapterViewListener {
        void selectLang(ContentEntry contentEntry);
    }


    @NonNull
    @Override
    public LangHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_entry_lang, parent, false);
        return new LangHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LangHolder holder, int position) {
        ContentEntry entry = data.get(position);
        holder.entryLang.setText(entry.getPrimaryLanguage());
        holder.entryLang.setOnClickListener(view -> listener.selectLang(entry));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class LangHolder extends RecyclerView.ViewHolder {
        public final Button entryLang;

        public LangHolder(View view) {
            super(view);
            entryLang = (Button) view;
        }

        @Override
        public String toString() {
            return entryLang.getText().toString();
        }
    }


    private class VIEW_TYPES {
        public static final int Header = 1;
        public static final int Normal = 2;
        public static final int Footer = 3;
    }

}
