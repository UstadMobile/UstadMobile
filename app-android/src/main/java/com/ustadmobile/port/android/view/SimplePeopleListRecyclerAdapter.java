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
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.lib.db.entities.Person;

/**
 * A Simple recycler adapter for a dead simple list of Students.
 */

public class SimplePeopleListRecyclerAdapter extends
        PagedListAdapter<Person,
                SimplePeopleListRecyclerAdapter.ClazzStudentViewHolder> {

    Context theContext;
    Fragment theFragment;
    CommonHandlerPresenter mPresenter;


    protected class ClazzStudentViewHolder extends RecyclerView.ViewHolder {
        protected ClazzStudentViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SimplePeopleListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Person> diffCallback,
                                               Context context, Fragment fragment) {
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
    }

    protected SimplePeopleListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Person> diffCallback,
                                              Context context, Fragment fragment,
                                              CommonHandlerPresenter presenter) {
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        mPresenter = presenter;
    }

    protected SimplePeopleListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Person> diffCallback,
                                              Context context) {
        super(diffCallback);
        theContext = context;
    }

    protected SimplePeopleListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Person> diffCallback,
                                              Context context, CommonHandlerPresenter presenter) {
        super(diffCallback);
        theContext = context;
        mPresenter = presenter;
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    @NonNull
    @Override
    public SimplePeopleListRecyclerAdapter.ClazzStudentViewHolder
            onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View clazzStudentListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_peoplelist, parent, false);
        return new SimplePeopleListRecyclerAdapter.ClazzStudentViewHolder(clazzStudentListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(
            @NonNull SimplePeopleListRecyclerAdapter.ClazzStudentViewHolder holder, int position) {


        Person thisPerson = getItem(position);
        String studentName;
        if(thisPerson == null){
            studentName = "Student";
        }else{
            studentName = thisPerson.getFirstNames() + " " +
                    thisPerson.getLastName();
        }

        TextView studentEntry = (TextView)holder.itemView
                .findViewById(R.id.item_peoplelist_name);
        studentEntry.setText(studentName);


        studentEntry.setOnClickListener(v -> mPresenter.handleCommonPressed(thisPerson.getPersonUid()));

    }
}
