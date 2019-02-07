package com.ustadmobile.port.android.view;

import android.app.Activity;
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
import com.ustadmobile.lib.db.entities.SelQuestion;

public class SocialNominationQuestionRecyclerAdapter extends
        PagedListAdapter<SelQuestion,
                SocialNominationQuestionRecyclerAdapter.SELQuestionViewHolder> {


    Context theContext;
    Activity theActivity;


    protected class SELQuestionViewHolder extends RecyclerView.ViewHolder {
        protected SELQuestionViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SocialNominationQuestionRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SelQuestion> diffCallback,
            Context context, Activity activity) {

        super(diffCallback);
        theContext = context;
        theActivity = activity;
    }

    protected SocialNominationQuestionRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SelQuestion> diffCallback,
                                              Context context) {
        super(diffCallback);
        theContext = context;
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    @NonNull
    @Override
    public SocialNominationQuestionRecyclerAdapter.SELQuestionViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View clazzStudentListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_questionlist, parent, false);
        return new SocialNominationQuestionRecyclerAdapter.SELQuestionViewHolder(
                clazzStudentListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(
            @NonNull SocialNominationQuestionRecyclerAdapter.SELQuestionViewHolder holder,
            int position) {


        SelQuestion thisQuestion = getItem(position);
        String questionString;
        if(thisQuestion == null){
            questionString = "Student";
        }else{
            questionString = thisQuestion.getQuestionText();
        }

        ((TextView)holder.itemView
                .findViewById(R.id.item_questionlist_name))
                .setText(questionString);


        //TODO: Figure how to add click listener for different use cases.

    }
}
