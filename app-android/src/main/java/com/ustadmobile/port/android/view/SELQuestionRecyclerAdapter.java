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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.core.controller.SELQuestionSetDetailPresenter;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;

public class SELQuestionRecyclerAdapter  extends PagedListAdapter<SocialNominationQuestion,
        SELQuestionRecyclerAdapter.SELQuestionViewHolder> {

    Context theContext;
    Activity theActivity;
    private SELQuestionSetDetailPresenter mPresenter;

    @NonNull
    @Override
    public SELQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sel_question, parent, false);
        return new SELQuestionRecyclerAdapter.SELQuestionViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SELQuestionViewHolder holder, int position) {

        SocialNominationQuestion theQuestion = getItem(position);
        TextView questionTitle =
                holder.itemView.findViewById(R.id.item_sel_question_title);
        TextView questionType =
                holder.itemView.findViewById(R.id.item_sel_question_type);

        questionTitle.setText(theQuestion.getQuestionText());

        //TODO: Get question type
        questionType.setText(" ");


        //Options to Edit/Delete every schedule in the list
        ImageView optionsImageView =
                holder.itemView.findViewById(R.id.item_sel_question_secondary_menu_imageview);
        optionsImageView.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleQuestionEdit(theQuestion.getSocialNominationQuestionUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleQuestionDelete(theQuestion.getSocialNominationQuestionUid());
                    return true;
                } else {
                    return false;
                }
            });
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule);

            //displaying the popup
            popup.show();
        });

    }

    protected class SELQuestionViewHolder extends RecyclerView.ViewHolder {
        protected SELQuestionViewHolder(View itemView){super(itemView);}
    }

    protected SELQuestionRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SocialNominationQuestion> diffCallback,
            Context context) {
        super(diffCallback);
        theContext = context;
    }
}
