package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionSetDetailPresenter;
import com.ustadmobile.core.db.dao.SelQuestionDao;
import com.ustadmobile.lib.db.entities.SelQuestion;

public class SELQuestionRecyclerAdapter  extends PagedListAdapter<SelQuestion,
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

        SelQuestion theQuestion = getItem(position);
        TextView questionTitle =
                holder.itemView.findViewById(R.id.item_sel_question_title);
        TextView questionType =
                holder.itemView.findViewById(R.id.item_sel_question_type);

        questionTitle.setText(theQuestion.getQuestionText());

        switch(theQuestion.getQuestionType()){
            case SelQuestionDao
                    .SEL_QUESTION_TYPE_NOMINATION:
                questionType.setText(theActivity.getText(R.string.sel_question_type_nomination));
                break;
            case SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE:
                questionType.setText(theActivity.getText(R.string.sel_question_type_multiple_choise));
                break;
            case SelQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT:
                questionType.setText(theActivity.getText(R.string.sel_question_type_free_text));
                break;
            default:
                break;
        }

        ConstraintLayout theWholeThang = holder.itemView.findViewById(R.id.item_sel_question_cl);
        theWholeThang.setOnClickListener(view ->
        {
            if(theQuestion != null)
                mPresenter.goToQuestionDetail(theQuestion);
        });

        //Options to Edit/Delete every schedule in the list
        ImageView optionsImageView =
                holder.itemView.findViewById(R.id.item_sel_question_secondary_menu_imageview);
        optionsImageView.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleQuestionEdit(theQuestion);
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleQuestionDelete(theQuestion.getSelQuestionUid());
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
            @NonNull DiffUtil.ItemCallback<SelQuestion> diffCallback,
            Context context, Activity activity, SELQuestionSetDetailPresenter presenter) {
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        mPresenter = presenter;
    }
}
