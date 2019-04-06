package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectProducerPresenter;

import com.ustadmobile.lib.db.entities.Person;

public class SelectProducerRecyclerAdapter extends
        PagedListAdapter<Person,
                SelectProducerRecyclerAdapter.SelectProducerViewHolder> {

    Context theContext;
    Activity theActivity;
    SelectProducerPresenter mPresenter;

    @NonNull
    @Override
    public SelectProducerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_simple, parent, false);
        return new SelectProducerViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SelectProducerViewHolder holder, int position) {

        Person entity = getItem(position);
        String name = entity.getFirstNames() + " " + entity.getLastName();
        TextView title = holder.itemView.findViewById(R.id.item_title_simple_title);
        title.setText(name);

        title.setOnClickListener(v -> mPresenter.handleClickProducer(entity.getPersonUid()));

    }

    protected class SelectProducerViewHolder extends RecyclerView.ViewHolder {
        protected SelectProducerViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SelectProducerRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<Person> diffCallback,
            SelectProducerPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
