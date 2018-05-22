package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PersonListView;
import com.ustadmobile.lib.db.entities.Person;

/**
 * Created by mike on 3/8/18.
 */

public class PersonListFragment extends UstadBaseFragment implements PersonListView{

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_person_list, container, false);


        return rootView;
    }

    @Override
    public void setProvider(UmProvider<Person> provider) {

    }


}
