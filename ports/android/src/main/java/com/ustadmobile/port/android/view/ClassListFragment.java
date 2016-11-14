package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ustadmobile.core.model.AttendanceClassEntity;
import com.ustadmobile.core.view.ClassListView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import com.ustadmobile.core.controller.ClassListController;
import android.widget.AdapterView;

import com.toughra.ustadmobile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClassListFragment extends UstadBaseFragment implements ClassListView, View.OnClickListener, AdapterView.OnItemClickListener{

    private RecyclerView mRecyclerView;

    private EntityCardAdapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private List<AttendanceClassEntity> mClassListEntities;

    private ClassListController mController;

    public ClassListFragment() {
        // Required empty public constructor
    }

    public static ClassListFragment newInstance(Bundle args) {
        ClassListFragment fragment = new ClassListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = ClassListController.makeControllerForView(this);
        setBaseController(mController);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_class_list, container,
                false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.fragment_class_list_recyclerview);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new EntityCardAdapter(mClassListEntities);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnEntityClickListener(this);

        return rootView;
    }


    @Override
    public void setClassList(String[] classList) {
        mClassListEntities = new ArrayList<>();
        for(int i = 0; i  < classList.length; i++) {
            mClassListEntities.add(new AttendanceClassEntity(classList[i]));
        }
    }

    @Override
    public void setClassStatus(int index, int statusCode, String statusMessage) {

    }

    @Override
    public void onClick(View view) {
        if(view instanceof EntityCard) {
            int position = mClassListEntities.indexOf(((EntityCard)view).getEntity());
            mController.handleClassSelected(position);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.handleClassSelected(position);
    }

}
