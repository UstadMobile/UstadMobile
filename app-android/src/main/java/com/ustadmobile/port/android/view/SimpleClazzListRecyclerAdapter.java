package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * Simple Clazz List recycler adapter -  simple mini recycler adapter just to show a list of classes
 * assigned to this person in the person detail .
 */
class SimpleClazzListRecyclerAdapter
        extends PagedListAdapter<ClazzWithNumStudents,
        SimpleClazzListRecyclerAdapter.ClazzLogDetailViewHolder> {

    Context theContext;

    class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
        ClazzLogDetailViewHolder(View itemView){
            super(itemView);
        }
    }

    SimpleClazzListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<ClazzWithNumStudents> diffCallback, Context context){
        super(diffCallback);
        theContext = context;
    }

    @NonNull
    @Override
    public ClazzLogDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View clazzLogDetailListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_clazzlist_clazz_simple, parent, false);
        return new ClazzLogDetailViewHolder(clazzLogDetailListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * Every item in the recycler view will have set its colors if no attendance status is set.
     * every attendance button will have it-self mapped to tints on activation.
     *
     * @param holder    View holder
     * @param position  The position
     */
    @Override
    public void onBindViewHolder(@NonNull ClazzLogDetailViewHolder holder, int position){
        ClazzWithNumStudents thisClazz = getItem(position);

        assert thisClazz != null;
        ((TextView)holder.itemView.findViewById(R.id.item_clazzlist_clazz_simple_clazz_name))
                .setText(thisClazz.getClazzName());

    }
}