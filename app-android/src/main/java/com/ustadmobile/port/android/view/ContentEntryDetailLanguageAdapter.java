package com.ustadmobile.port.android.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.toughra.ustadmobile.R;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;

import java.util.List;

public class ContentEntryDetailLanguageAdapter extends RecyclerView.Adapter<ContentEntryDetailLanguageAdapter.LangHolder> {


    private final List<ContentEntryRelatedEntryJoinWithLanguage> data;
    private final long entryUid;
    private AdapterViewListener listener;

    public ContentEntryDetailLanguageAdapter(List<ContentEntryRelatedEntryJoinWithLanguage> data, AdapterViewListener listener, long entryUuid){
        this.data = data;
        this.listener = listener;
        this.entryUid = entryUuid;
    }

    protected interface AdapterViewListener {
        void selectContentEntryOfLanguage(long contentEntryUid);
    }


    @NonNull
    @Override
    public LangHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_entry_lang, parent, false);
        return new LangHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LangHolder holder, int position) {
        ContentEntryRelatedEntryJoinWithLanguage entry = data.get(position);
        holder.entryLang.setText(entry.getLanguageName());
        holder.entryLang.setOnClickListener(view -> listener.selectContentEntryOfLanguage(entry.getCerejContentEntryUid() == entryUid ? entry.getCerejRelatedEntryUid() : entry.getCerejContentEntryUid()));
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
