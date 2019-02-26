package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonDetailEnrollClazzPresenter;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;

import java.util.HashMap;

/**
 * The ClazzList Recycler Adapter used here.
 */
public class ClazzListEnrollPersonRecyclerAdapter extends
        PagedListAdapter<ClazzWithEnrollment,
                ClazzListEnrollPersonRecyclerAdapter.ClazzViewHolder> {

    Context theContext;
    private Activity theActivity;
    private PersonDetailEnrollClazzPresenter thePresenter;

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Boolean> checkBoxHM = new HashMap<>();

    class ClazzViewHolder extends RecyclerView.ViewHolder {

        ClazzViewHolder(View itemView) {
            super(itemView);
        }
    }

    ClazzListEnrollPersonRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<ClazzWithEnrollment>
                    diffCallback, Context context, Activity activity,
            PersonDetailEnrollClazzPresenter mPresenter) {
        super(diffCallback);
        theContext = context;
        theActivity = activity;
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

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * @param holder The view holder
     * @param position The position of the item
     */
    @Override
    public void onBindViewHolder  (@NonNull ClazzViewHolder holder, int position) {
        ClazzWithEnrollment clazz = getItem(position);
        assert clazz != null;
        String numStudentsText = clazz.getNumStudents() + " " + theActivity.getResources()
                .getText(R.string.students_literal).toString();
        ((TextView)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_title))
                .setText(clazz.getClazzName());
        ((TextView)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_numstudents_text))
                .setText(numStudentsText);

        CheckBox checkBox = holder.itemView.findViewById(
                R.id.item_clazz_list_enroll_person_checkbox);

        //To preserve checkboxes, add this enrollment to the Map.
        checkBoxHM.put(clazz.getClazzUid(), clazz.getEnrolled());

        checkBox.setChecked(checkBoxHM.get(clazz.getClazzUid()));

        //Add a change listener to the checkbox
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                checkBox.setChecked(isChecked));

        checkBox.setOnClickListener(v -> {
            final boolean isChecked = checkBox.isChecked();
            thePresenter.handleToggleClazzChecked(clazz.getClazzUid(), clazz.getPersonUid(),
                    isChecked);
        });


        holder.itemView.setOnClickListener((view) -> thePresenter.handleClickClazz());

    }
}