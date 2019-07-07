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
import com.ustadmobile.core.controller.SelectMultiplePeoplePresenter;
import com.ustadmobile.lib.db.entities.Person;

import java.util.ArrayList;
import java.util.List;

public class PersonListReturnSelectedRecyclerAdapter extends PagedListAdapter<Person,
        PersonListReturnSelectedRecyclerAdapter.PersonViewHolder> {

    Context theContext;
    private Fragment theFragment;
    private SelectMultiplePeoplePresenter thePresenter;
    private List<Long> selectedPeople;

    class PersonViewHolder extends RecyclerView.ViewHolder {
        PersonViewHolder(View itemView){super(itemView);}
    }

    PersonListReturnSelectedRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<Person>
                    diffCallback, Context context, Fragment fragment,
            SelectMultiplePeoplePresenter mPresenter) {
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
                        .inflate(R.layout.item_person_checked, parent, false);
        return new PersonViewHolder(personListItem);
    }

    @Override
    public void onBindViewHolder  (@NonNull PersonViewHolder holder, int position) {
        Person person = getItem(position);
        assert person != null;

        TextView title = holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_title);
        title.setText(person.getFirstNames() + " " + person.getLastName());

        CheckBox checkBox = holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_checkbox);
        checkBox.setText("");

        //checkBox.setChecked(???);
        if(selectedPeople.contains(person.getPersonUid())){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                {
                    if(isChecked){
                        thePresenter.addToPeople(person);
                    }else{
                        thePresenter.removePeople(person);
                    }
                }
                );

    }

}
