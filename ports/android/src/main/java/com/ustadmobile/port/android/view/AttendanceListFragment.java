package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AttendanceListController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.LoadControllerThread;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.core.view.AttendanceListView;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public class AttendanceListFragment extends EntityListFragment implements AttendanceListView, ControllerReadyListener, View.OnClickListener{

    private AttendanceListController mAttendanceListController;

    private FloatingActionButton cameraButton;

    private FloatingActionButton directEntryButton;

    private boolean isLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void controllerReady(UstadController controller, int flags) {
        mAttendanceListController = (AttendanceListController)controller;
        mAttendanceListController.setView(this);
        setEntityList(mAttendanceListController.getList());
        isLoading = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        FloatingActionMenu fMenu = getFloatingActionMenu();
        fMenu.setVisibility(View.VISIBLE);

        cameraButton = new FloatingActionButton(getContext());
        cameraButton.setImageDrawable(ContextCompat.getDrawable(getContext(),
                R.drawable.ic_photo_camera_white_18dp));
        cameraButton.setLabelText("Snap Sheet");
        cameraButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        cameraButton.setOnClickListener(this);
        fMenu.addMenuButton(cameraButton, 0);

        directEntryButton = new FloatingActionButton(getContext());
        directEntryButton.setImageDrawable(ContextCompat.getDrawable(getContext(),
                R.drawable.ic_phone_android_white_18dp));
        directEntryButton.setLabelText("Direct Entry");
        directEntryButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        directEntryButton.setOnClickListener(this);
        fMenu.addMenuButton(directEntryButton, 1);

        return rootView;
    }

    @Override
    public void onStart() {
        isLoading = true;
        AttendanceListController.makeControllerForView(this, UMAndroidUtil.bundleToHashtable(getArguments()), this);
        super.onStart();
    }

    public void setEntityList(List<? extends ListableEntity> list)  {
        super.setEntityList(list);
        mAdapter.setEntityIconId(R.drawable.ic_today_black_48dp);
        mAdapter.setDetailTextVisible(true);
    }

    @Override
    public void onClick(View view) {
        if(view == cameraButton) {
            floatingActionMenu.close(true);
            mAttendanceListController.handleClickSnapSheet();
        }else if(view == directEntryButton) {
            floatingActionMenu.close(true);
            mAttendanceListController.handleClickDirectEntry();
        }
    }

    @Override
    public void addItem(ListableEntity item) {

    }

    @Override
    public void invalidateItem(ListableEntity item) {

    }

    @Override
    public void removeAllItems() {

    }


    @Override
    public void updateStatus(String hostedStatementId, int status, final String statusMessage) {
        final EntityCard card = mAdapter.getCardByEntityId(hostedStatementId);
        if(card != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    card.setStatusIcon(card.getEntity().getStatusIconCode());
                    card.setStatusText(statusMessage);
                }
            });
        }
    }
}
