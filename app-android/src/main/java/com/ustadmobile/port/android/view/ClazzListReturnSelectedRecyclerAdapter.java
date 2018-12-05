package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

public class ClazzListReturnSelectedRecyclerAdapter extends PagedListAdapter<ClazzWithNumStudents,
        ClazzListReturnSelectedRecyclerAdapter.ClazzViewHolder> {

    Context theContext;
    private Fragment theFragment;
    private CommonHandlerPresenter thePresenter;

    class ClazzViewHolder extends RecyclerView.ViewHolder {
        ClazzViewHolder(View itemView){super(itemView);}
    }

    ClazzListReturnSelectedRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<ClazzWithNumStudents>
                    diffCallback, Context context, Fragment fragment,
            CommonHandlerPresenter mPresenter) {
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        thePresenter = mPresenter;
    }


    @NonNull
    @Override
    public ClazzViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View clazzListItem =
                LayoutInflater.from(theContext)
                        .inflate(R.layout.item_clazz_list_enroll_person, parent, false);
        return new ClazzViewHolder(clazzListItem);
    }

    @Override
    public void onBindViewHolder  (@NonNull ClazzViewHolder holder, int position) {
        ClazzWithNumStudents clazz = getItem(position);
        assert clazz != null;
        String numStudentsText = clazz.getNumStudents() + " " + theFragment.getResources()
                .getText(R.string.students_literal).toString();
        ((TextView)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_title))
                .setText(clazz.getClazzName());
        ((TextView)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_numstudents_text))
                .setText(numStudentsText);

        ((CheckBox)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_checkbox)).setText("");
        //TODO: Handle
//        ((CheckBox)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_checkbox))
//                .setChecked(false);

        holder.itemView.setOnClickListener((view) -> thePresenter.handleCommonPressed(-1));

    }

}
