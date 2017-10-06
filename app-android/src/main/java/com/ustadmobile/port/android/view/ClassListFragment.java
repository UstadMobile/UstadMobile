package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.ClassListController;
import com.ustadmobile.port.sharedse.model.AttendanceClass;
import com.ustadmobile.port.sharedse.view.ClassListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClassListFragment extends UstadBaseFragment implements ClassListView, View.OnClickListener, AdapterView.OnItemClickListener, ControllerReadyListener{

    private RecyclerView mRecyclerView;

    private EntityCardAdapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private List<AttendanceClass> mClassListEntities;

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
        ClassListController.makeControllerForView(this, UMAndroidUtil.bundleToHashtable(getArguments()),
            this);
    }

    @Override
    public void controllerReady(UstadController controller, int flags) {
        this.mController = (ClassListController)controller;
        setBaseController(mController);
    }


    @Override
    public void setClassList(final AttendanceClass[] classList) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mClassListEntities = new ArrayList<>();
                for(int i = 0; i  < classList.length; i++) {
                    mClassListEntities.add(classList[i]);
                }
            }
        });
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

        //setClassList has already been called
        mAdapter = new EntityCardAdapter(mClassListEntities);
        mAdapter.setEntityIconId(R.drawable.ic_people_black_48dp);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnEntityClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if(view instanceof EntityCard) {
            int position = mClassListEntities.indexOf(((EntityCard)view).getEntity());
            mController.handleClassSelected(position);
        }
    }

    @Override
    public void setClassStatus(String classId, int statusCode, String statusMessage) {
        final EntityCard card = mAdapter.getCardByEntityId(classId);
        if(card != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    AttendanceClass cls = (AttendanceClass)card.getEntity();
                    card.setStatusText(cls.getStatusText(getContext()));
                    card.setStatusIcon(cls.getStatusIconCode());
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.handleClassSelected(position);
    }

}
