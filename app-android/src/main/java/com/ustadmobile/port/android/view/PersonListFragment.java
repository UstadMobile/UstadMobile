package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.PersonListController;
import com.ustadmobile.port.sharedse.view.PersonListView;

import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public class PersonListFragment extends EntityListFragment implements PersonListView, ControllerReadyListener, SwipeRefreshLayout.OnRefreshListener {

    private PersonListController mPersonListController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PersonListController.makeControllerForView(this, UMAndroidUtil.bundleToHashtable(getArguments()),
                this);
    }


    @Override
    public void controllerReady(UstadController controller, int flags) {
        mPersonListController = (PersonListController)controller;
        setEntityList(mPersonListController.getList());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        refreshLayout.setEnabled(true);
        refreshLayout.setOnRefreshListener(this);
        return view;
    }


    public void setEntityList(List<? extends ListableEntity> list)  {
        super.setEntityList(list);
        mAdapter.setDetailTextVisible(false);
        mAdapter.setStatusVisible(false);
        mAdapter.setEntityIconId(R.drawable.ic_person_black_24dp);
    }

    @Override
    public void onRefresh() {
        mPersonListController.handleRefresh();
    }

    @Override
    public void setRefreshing(boolean refreshingActive) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        });
    }
}
