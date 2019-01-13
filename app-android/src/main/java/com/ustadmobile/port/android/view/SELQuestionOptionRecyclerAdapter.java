package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionDetail2Presenter;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionOption;

import io.reactivex.annotations.NonNull;

public class SELQuestionOptionRecyclerAdapter extends
        PagedListAdapter<SocialNominationQuestionOption,
                SELQuestionOptionRecyclerAdapter.SELQuestionOptionViewHolder> {
    Context theContext;
    Activity theActivity;
    SELQuestionDetail2Presenter mPresenter;
    //The presenter mPresenter

    protected SELQuestionOptionRecyclerAdapter(
            @android.support.annotation.NonNull DiffUtil.ItemCallback<SocialNominationQuestionOption> diffCallback,
            Context context, Activity mActivity, SELQuestionDetail2Presenter thePresenter
            ) {
        super(diffCallback);
        theContext = context;
        theActivity = mActivity;
        mPresenter = thePresenter;
    }

    @NonNull
    @Override
    public SELQuestionOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sel_question_option, parent, false);
        return new SELQuestionOptionRecyclerAdapter.SELQuestionOptionViewHolder(list);

    }

    @Override
    public void onBindViewHolder(
            @android.support.annotation.NonNull SELQuestionOptionViewHolder holder, int position) {
        SocialNominationQuestionOption questionOption = getItem(position);
        TextView questionOptionTitle =
                holder.itemView.findViewById(R.id.item_sel_question_option_text);
        questionOptionTitle.setText(questionOption.getOptionText());

        ImageView optionsImageView =
                holder.itemView.findViewById(R.id.item_sel_question_option_secondary_menu_imageview);
        optionsImageView.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleQuestionOptionEdit(
                            questionOption.getSelQuestionOptionQuestionUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleQuestionOptionDelete(
                            questionOption.getSelQuestionOptionQuestionUid());
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

    protected class SELQuestionOptionViewHolder extends RecyclerView.ViewHolder {
        protected SELQuestionOptionViewHolder(View itemView){super(itemView);}
    }

}
