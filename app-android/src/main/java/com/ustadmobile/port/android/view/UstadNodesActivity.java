package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.network.NetworkNode;
import com.ustadmobile.port.sharedse.network.NetworkNodeListener;

import java.util.ArrayList;

public class UstadNodesActivity extends UstadBaseActivity implements NetworkNodeListener {


    private RecyclerView allNodesList;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private NodeListAdapter nodeListAdapter;
    private int nodeCounter=0;
    private NetworkManagerAndroid networkManagerAndroid;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_ustad_nodes);
        setUMToolbar(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView)findViewById(R.id.toolbarTitle)).setText(UstadMobileSystemImpl.getInstance().getString(MessageIDConstants.nodeListTitle));
        allNodesList= (RecyclerView) findViewById(R.id.allNodes);
        coordinatorLayout= (CoordinatorLayout) findViewById(R.id.coordinationLayout);
        snackbar = Snackbar
                .make(coordinatorLayout, "", Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        final TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Color.WHITE);
        networkManagerAndroid =(NetworkManagerAndroid) UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        nodeListAdapter=new NodeListAdapter();
        allNodesList.setLayoutManager(linearLayoutManager);
        allNodesList.setAdapter(nodeListAdapter);
       /* if(networkManagerAndroid.getNodeList().size()>0){
            nodeCounter= networkManagerAndroid.getNodeList().size();
            snackbar.show();
            textView.setText("Found "+nodeCounter+" super nodes");
        }else{
            textView.setText("Searching super nodes...");
            snackbar.show();
        }
        nodeListAdapter.setNodeList((ArrayList<NetworkNode>) networkManagerAndroid.getNodeList());
        ((NetworkManagerAndroid) UstadMobileSystemImplSE.getInstanceSE().getNetworkManager()).addNodeListener(this);*/
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
    public void nodeDiscovered(NetworkNode node) {
       /* nodeListAdapter.setNodeList((ArrayList<NetworkNode>) networkManagerAndroid.getNodeList());
        nodeListAdapter.notifyDataSetChanged();
        allNodesList.invalidate();*/
    }

    @Override
    public void nodeGone(NetworkNode node) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Device node adapter, for listing the available nodes
     */
    public class NodeListAdapter extends RecyclerView.Adapter<NodeListAdapter.NodeHolder> {

        private ArrayList<NetworkNode> nodeLists;
        private LayoutInflater inflater;
        public NodeListAdapter() {

            inflater = LayoutInflater.from(getApplicationContext());
        }

        @Override
        public NodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            return new NodeHolder(inflater.inflate(R.layout.single_node_view, parent, false));
        }

        @Override
        public void onBindViewHolder(final NodeListAdapter.NodeHolder holder, int position) {

            /*holder.nodeAddress.setText(getNodeList().get(holder.getAdapterPosition()).getNodeMacAddress());
            holder.nodeName.setText(getNodeList().get(holder.getAdapterPosition()).getNetworkSSID());
            holder.nodeStatus.setBackgroundResource(getDeviceStatus(getNodeList().get(holder.getAdapterPosition()).getStatus()));

            holder.nodeHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    UstadMobileSystemImpl.getInstance().setAppPref("net_ssid",
                            getNodeList().get(holder.getAdapterPosition()).getNetworkSSID(),
                            getApplicationContext());

                    DnsSdTxtRecord nodeRecord= networkManagerAndroid.getService(getContext()).getWifiDirectHandlerAPI()
                            .getDnsSdTxtRecordMap().get(getNodeList().get(holder.getAdapterPosition()).getNodeMacAddress());
                    networkManagerAndroid.getService(getContext()).getWifiDirectHandlerAPI().connectToNoPromptService(nodeRecord);

                }
            });*/
        }

        void setNodeList(ArrayList<NetworkNode> nodeList) {
            this.nodeLists = nodeList;
        }

        ArrayList<NetworkNode> getNodeList() {

            return nodeLists;
        }

        @Override
        public int getItemCount() {
            return nodeLists.size();
        }



        class NodeHolder extends RecyclerView.ViewHolder{

            CardView nodeHolder;
            TextView nodeName,nodeAddress,nodeStatus;
            NodeHolder(View itemView) {
                super(itemView);
                nodeName= (TextView) itemView.findViewById(R.id.nodeName);
                nodeStatus= (TextView) itemView.findViewById(R.id.nodeStatus);
                nodeHolder= (CardView) itemView.findViewById(R.id.nodeHolder);
            }
        }

    }


    /**
     *  Get readable status of the device from its status code
     * @param statusCode
     * @return
     */
    private static int getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return android.R.color.holo_green_dark;
            case WifiP2pDevice.INVITED:
                return android.R.color.holo_blue_dark;
            case WifiP2pDevice.FAILED:
                return android.R.color.holo_red_dark;
            case WifiP2pDevice.AVAILABLE:
                return android.R.color.holo_green_light;
            case WifiP2pDevice.UNAVAILABLE:
                return R.color.text_primary;
            default:
                return R.color.text_secondary;

        }
    }
}
