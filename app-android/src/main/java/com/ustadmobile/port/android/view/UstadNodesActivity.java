package com.ustadmobile.port.android.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.p2p.NodeListAdapter;
import com.ustadmobile.port.android.util.P2PAndroidUtils;

import java.util.ArrayList;

import edu.rit.se.wifibuddy.P2PNode;
import edu.rit.se.wifibuddy.WifiDirectHandler;

public class UstadNodesActivity extends UstadBaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView allNodesList;
    private NodeListAdapter nodeListAdapter;
    private SwipeRefreshLayout refreshNodeList;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ustad_nodes);
        setUMToolbar(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.MESSAGE_RECEIVED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);
        filter.addAction(WifiDirectHandler.Action.P2P_GROUP_FORMED);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equalsIgnoreCase(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE)){
                    refreshNodeList.setRefreshing(false);
                    nodeListAdapter.setNodeList(P2PAndroidUtils.wifiDirectHandler.getP2PNodeList());
                    nodeListAdapter.notifyDataSetChanged();
                    allNodesList.invalidate();

                }

                Toast.makeText(getApplicationContext(),intent.getAction(),Toast.LENGTH_LONG).show();

            }
        }, filter);

        P2PAndroidUtils.wifiDirectHandler.continuouslyDiscoverServices();




        ((TextView)findViewById(R.id.toolbarTitle)).setText(UstadMobileSystemImpl.getInstance().getString(MessageIDConstants.nodeListTitle));
        allNodesList= (RecyclerView) findViewById(R.id.allNodes);
        refreshNodeList= (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_node_list);

        refreshNodeList.setOnRefreshListener(this);
        refreshNodeList.setColorSchemeResources(R.color.primary_dark, R.color.accent, R.color.text_primary);

        refreshNodeList.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        nodeListAdapter=new NodeListAdapter(getApplicationContext());
        allNodesList.setLayoutManager(linearLayoutManager);
        allNodesList.setAdapter(nodeListAdapter);
        nodeListAdapter.setNodeList(new ArrayList<P2PNode>());




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        refreshNodeList.setRefreshing(true);
    }


}
