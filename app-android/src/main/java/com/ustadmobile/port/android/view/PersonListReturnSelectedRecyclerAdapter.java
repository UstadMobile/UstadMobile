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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectPeopleDialogPresenter;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.ArrayList;
import java.util.List;

public class PersonListReturnSelectedRecyclerAdapter extends PagedListAdapter<PersonWithEnrollment,
        PersonListReturnSelectedRecyclerAdapter.PersonViewHolder> {

    Context theContext;
    private Fragment theFragment;
    private SelectPeopleDialogPresenter thePresenter;
    private List<Long> selectedPeople;

    class PersonViewHolder extends RecyclerView.ViewHolder {
        PersonViewHolder(View itemView){super(itemView);}
    }

    PersonListReturnSelectedRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment>
                    diffCallback, Context context, Fragment fragment,
            SelectPeopleDialogPresenter mPresenter) {
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        thePresenter = mPresenter;
        selectedPeople = mPresenter.getSelectedPeopleList();
        if(selectedPeople == null){
            selectedPeople = new ArrayList<>();
        }
    }


    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View personListItem =
                LayoutInflater.from(theContext)
                        .inflate(R.layout.item_clazz_list_enroll_person, parent, false);
        return new PersonViewHolder(personListItem);
    }

    @Override
    public void onBindViewHolder  (@NonNull PersonViewHolder holder, int position) {
        PersonWithEnrollment person = getItem(position);
        assert person != null;

        //TODO

    }

}
