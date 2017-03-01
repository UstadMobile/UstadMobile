package com.ustadmobile.port.android.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.p2p.P2PManagerAndroid;
import com.ustadmobile.port.sharedse.p2p.P2PNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.WifiDirectHandler;

public class UstadNodesActivity extends UstadBaseActivity{


    private RecyclerView allNodesList;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private NodeListAdapter nodeListAdapter;
    private int nodeCounter=0;
    private static final String NO_PROMPT_NETWORK_PASS="passphrase",
            NO_PROMPT_NETWORK_NAME="networkName",
            DEVICE_MAC_ADDRESS="deviceAddress",
            DEVICE_STATUS="deviceStatus";

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


        P2PManagerAndroid android=new P2PManagerAndroid();
        android.getServiceConnectionMap();
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        nodeListAdapter=new NodeListAdapter(getApplicationContext());
        allNodesList.setLayoutManager(linearLayoutManager);
        allNodesList.setAdapter(nodeListAdapter);
        if(getNodes().size()>0){
            nodeCounter=getNodes().size();
            snackbar.show();
            textView.setText("Found "+nodeCounter+" super nodes");
        }else{
            textView.setText("Searching super nodes...");
            snackbar.show();
        }
        nodeListAdapter.setNodeList(getNodes());

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE)){
                    if(nodeCounter<getNodes().size()){
                        nodeCounter=getNodes().size();
                        textView.setText("Found "+getNodes().size()+" super nodes");
                        snackbar.show();
                    }

                    nodeListAdapter.setNodeList(getNodes());
                    nodeListAdapter.notifyDataSetChanged();
                    allNodesList.invalidate();
                }

            }
        },filter);


    }

    /**
     * Get the list of the available nodes from shared preference of which will be
     * update using broadcast receiver from P2pServiceAndroid
     * @return
     */
    ArrayList<P2PNode> getNodes(){
        String nodes=UstadMobileSystemImpl.getInstance().getAppPref("devices",this);
        ArrayList<P2PNode> nodeList=new ArrayList<>();

        try{
            JSONObject jsonObject=new JSONObject(nodes);
            Iterator<String> macAddresses = jsonObject.keys();
            String macAddr;
            Object keyVal;
            JSONObject currentNode;
            while(macAddresses.hasNext()) {
                macAddr = macAddresses.next();
                keyVal = jsonObject.get(macAddr);
                if(keyVal instanceof  JSONObject) {
                    currentNode = (JSONObject)keyVal;
                    P2PNode node=new P2PNode(currentNode.getString(DEVICE_MAC_ADDRESS));
                    node.setNetworkPass(currentNode.getString(NO_PROMPT_NETWORK_PASS));
                    node.setNetworkSSID(currentNode.getString(NO_PROMPT_NETWORK_NAME));
                    node.setNodeAddress(currentNode.getString(DEVICE_MAC_ADDRESS));
                    node.setStatus(Integer.parseInt(currentNode.getString(DEVICE_STATUS)));
                    nodeList.add(node);
                }
            }


            return nodeList;
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
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


    /**
     * Device node adapter, for listing the available nodes
     */
    public class NodeListAdapter extends RecyclerView.Adapter<NodeListAdapter.NodeHolder> {

        private ArrayList<P2PNode> nodeLists;
        private LayoutInflater inflater;
        private Context context;
        public NodeListAdapter(Context context) {
            this.context=context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public NodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new NodeHolder(inflater.inflate(R.layout.single_node_view, parent, false));
        }

        @Override
        public void onBindViewHolder(final NodeListAdapter.NodeHolder holder, int position) {

            holder.nodeAddress.setText(getNodeList().get(holder.getAdapterPosition()).getNodeAddress());
            holder.nodeName.setText(getNodeList().get(holder.getAdapterPosition()).getNetworkSSID());
            holder.nodeStatus.setBackgroundResource(getDeviceStatus(getNodeList().get(holder.getAdapterPosition()).getStatus()));

            holder.nodeHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    UstadMobileSystemImpl.getInstance().setAppPref("net_ssid",
                            getNodeList().get(holder.getAdapterPosition()).getNetworkSSID(),
                            getApplicationContext());

                    connectNetwork(
                            getNodeList().get(holder.getAdapterPosition()).getNetworkSSID(),
                            getNodeList().get(holder.getAdapterPosition()).getNetworkPass()
                            );
                }
            });
        }

        void setNodeList(ArrayList<P2PNode> nodeList) {
            this.nodeLists = nodeList;
        }

        ArrayList<P2PNode> getNodeList() {

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
                nodeAddress= (TextView) itemView.findViewById(R.id.nodeAddress);
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



    void connectNetwork(String SSID,String pasPhrase){


        UstadMobileSystemImpl.getInstance().setAppPref("mama","true",getApplicationContext());
        Log.d(WifiDirectHandler.TAG,"Connecting to the no prompt network");
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + SSID + "\"";
        configuration.preSharedKey = "\"" + pasPhrase+"\"";
        int netId = wifiManager.addNetwork(configuration);

        //disconnect form current network and connect to this one
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        Toast.makeText(this, "Initiating connection to " + SSID, Toast.LENGTH_LONG).show();
    }





}
