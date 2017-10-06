package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.SendCoursePresenter;
import com.ustadmobile.port.sharedse.view.SendCourseView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mike on 8/15/17.
 */

public class SendCourseDialogFragment extends UstadDialogFragment implements SendCourseView, View.OnClickListener, ReceiverDeviceView.OnClickCancelInviteListener {

    private RecyclerView receiversRecyclerView;

    private RecyclerView.Adapter recyclerViewAdapter;

    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private View rootView;

    private HashMap<View, String> receiverItemViewToIdMap = new HashMap<>();

    private HashMap<String, ReceiverDeviceView> receiverIdToItemMap = new HashMap<>();

    private HashMap<String, Integer> receiverIdsToStatusMap = new HashMap<>();

    private ArrayList<String> receiverNames = new ArrayList<>();

    private ArrayList<String> receiverIds =new ArrayList<>();

    private SendCoursePresenter mPresenter;

    private boolean receiversListEnabled = true;

    private class SendCourseRecyclerAdapter extends RecyclerView.Adapter<SendCourseRecyclerAdapter.ViewHolder>{

        public SendCourseRecyclerAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ReceiverDeviceView itemView;

            String receiverId;

            public ViewHolder(ReceiverDeviceView itemView) {
                super(itemView);
                this.itemView = itemView;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            ReceiverDeviceView receiverView = new ReceiverDeviceView(getContext());
            receiverView.setLayoutParams(lp);
            receiverView.setOnClickListener(SendCourseDialogFragment.this);
            receiverView.setOnCancelInviteListener(SendCourseDialogFragment.this);

            return new ViewHolder(receiverView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemView.setDeviceName(receiverNames.get(position));
            holder.receiverId = receiverIds.get(position);
            if(receiverItemViewToIdMap.containsKey(holder.itemView)){
                receiverItemViewToIdMap.remove(holder.itemView);
            }

            Integer deviceStatus = receiverIdsToStatusMap.get(holder.receiverId);
            Collection<ReceiverDeviceView> receiverIdToItemMapValues= receiverIdToItemMap.values();
            if(receiverIdToItemMapValues.contains(holder.itemView))
                receiverIdToItemMapValues.remove(holder.itemView);

            holder.itemView.setDeviceStatus(deviceStatus);
            holder.itemView.setEnabled(receiversListEnabled);

            receiverItemViewToIdMap.put(holder.itemView, holder.receiverId);
            receiverIdToItemMap.put(holder.receiverId, holder.itemView);
        }

        @Override
        public int getItemCount() {
            return receiverIds.size();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_send_course_dialog, container, false);

        receiversRecyclerView = (RecyclerView)rootView.findViewById(R.id.fragment_send_course_recycler_view);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        receiversRecyclerView.setLayoutManager(recyclerViewLayoutManager);
        receiversRecyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new SendCourseRecyclerAdapter();
        receiversRecyclerView.setAdapter(recyclerViewAdapter);

        mPresenter = new SendCoursePresenter(getContext(), UMAndroidUtil.bundleToHashtable(getArguments()),
                this);
        mPresenter.onCreate(null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().addContentView(rootView, params);
        getDialog().setTitle(R.string.share);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStop() {
        mPresenter.onStop();
        NetworkManagerAndroid nmAndroid = (NetworkManagerAndroid)UstadMobileSystemImpl.getInstance().getNetworkManager();
        nmAndroid.removeActiveWifiObject(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        mPresenter.onStart();
        NetworkManagerAndroid nmAndroid = (NetworkManagerAndroid)UstadMobileSystemImpl.getInstance().getNetworkManager();
        nmAndroid.addActiveWifiObject(this);
        super.onStart();
    }

    @Override
    public void addReceiver(String id, String name) {
        synchronized (receiverIds) {
            receiverIds.add(id);
            receiverNames.add(name);
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void clearReceivers() {
        synchronized (receiverIds) {
            receiverIds.clear();
            receiverNames.clear();
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeReceiver(String id) {
        synchronized (receiverIds) {
            int index = receiverIds.indexOf(id);
            receiverIds.remove(index);
            receiverNames.remove(index);
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void setReceivers(List<String> ids, List<String> names) {
        synchronized (receiverIds) {
            receiverIds.clear();
            receiverIds.addAll(ids);
            receiverNames.clear();
            receiverNames.addAll(names);
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        String deviceId = receiverItemViewToIdMap.get(view);
        if(deviceId != null){
            mPresenter.handleClickReceiver(deviceId);
        }
    }

    @Override
    public void onClickCancelInvite(ReceiverDeviceView src) {
        mPresenter.handleClickCancelInvite(src.getDeviceId());
    }

    @Override
    public void setReceiversListEnabled(boolean enabled) {
        receiversListEnabled = enabled;
        synchronized (receiverIds) {
            for(String receiverId : receiverIds) {
                View receiverView = receiverIdToItemMap.get(receiverId);
                if(receiverView == null)
                    continue;
                receiverView.setEnabled(enabled);
            }
        }
    }

    private void setViewEnabledRecursive(View view, boolean enabled) {
        view.setEnabled(enabled);
        if(view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            for(int i = 0; i < viewGroup.getChildCount(); i++) {
                setViewEnabledRecursive(viewGroup.getChildAt(i), enabled);
            }
        }
    }

    @Override
    public void setStatusText(String statusText) {
        TextView statusTextView = rootView.findViewById(R.id.fragment_send_course_status_text);
        statusTextView.setText(statusText);
    }


    @Override
    public void setReceiverStatus(String receiverId, int status) {
        receiverIdsToStatusMap.put(receiverId, status);
        ReceiverDeviceView receiverView = receiverIdToItemMap.get(receiverId);
        if(receiverView != null){
            receiverView.setDeviceStatus(status);
        }
    }
}
