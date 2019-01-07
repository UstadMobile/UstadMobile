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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionSetsPresenter;
import com.ustadmobile.core.tincan.Activity;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;

public class SELQuestionSetListRecyclerAdapter extends
        PagedListAdapter<SocialNominationQuestionSet,
                SELQuestionSetListRecyclerAdapter.SELQuestionSetsViewHolder> {

    Context theContext;
    Activity theActivity;
    SELQuestionSetsPresenter mPresenter;

    @NonNull
    @Override
    public SELQuestionSetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sel_question_set, parent, false);
        return new SELQuestionSetListRecyclerAdapter.SELQuestionSetsViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SELQuestionSetsViewHolder holder, int position) {

        SocialNominationQuestionSet theQuestionSet = getItem(position);
        TextView questionTitle =
                holder.itemView.findViewById(R.id.item_sel_question_set_question);
        TextView questionAmount =
                holder.itemView.findViewById(R.id.item_sel_question_set_number_of_questions);

        questionTitle.setText(theQuestionSet.getTitle());
        ConstraintLayout cl = holder.itemView.findViewById(R.id.item_sel_question_set_question_cl);
        cl.setOnClickListener(view ->
                mPresenter.handleGoToQuestionSet(theQuestionSet.getSocialNominationQuestionSetUid(),
                        theQuestionSet.getTitle()));


        //TODO: Get number of questions
        questionAmount.setText(" " + theContext.getText(R.string.questions).toString());
    }

    protected class SELQuestionSetsViewHolder extends RecyclerView.ViewHolder {
        protected SELQuestionSetsViewHolder(View itemView){super(itemView);}
    }

    protected SELQuestionSetListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SocialNominationQuestionSet> diffCallback,
            SELQuestionSetsPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }





}
