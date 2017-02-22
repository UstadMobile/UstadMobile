package com.ustadmobile.port.android.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.util.P2PAndroidUtils;
import com.ustadmobile.port.sharedse.p2p.P2PNode;


import java.util.ArrayList;

import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 15/02/2017.
 */

public class NodeListAdapter extends RecyclerView.Adapter<NodeHolder> {

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
    public void onBindViewHolder(final NodeHolder holder, int position) {

        holder.nodeAddress.setText(getNodeList().get(holder.getAdapterPosition()).getAddress()+" "+getNodeList().get(holder.getAdapterPosition()).getRecord().get(WifiDirectHandler.Keys.NO_PROMPT_NETWORK_PASS));
        holder.nodeName.setText(getNodeList().get(holder.getAdapterPosition()).getName());
        holder.nodeStatus.setBackgroundResource(getDeviceStatus(getNodeList().get(position).getStatus()));



        holder.nodeHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                P2PAndroidUtils.wifiDirectHandler.connectToNoPromptService(
                        nodeLists.get(holder.getAdapterPosition()).getRecord().get(WifiDirectHandler.Keys.NO_PROMPT_NETWORK_NAME),
                        nodeLists.get(holder.getAdapterPosition()).getRecord().get(WifiDirectHandler.Keys.NO_PROMPT_NETWORK_PASS));
            }
        });
    }


    public void setNodeList(ArrayList<P2PNode> nodeList) {
        this.nodeLists = nodeList;
    }


    public ArrayList<P2PNode> getNodeList() {
        return nodeLists;
    }

    @Override
    public int getItemCount() {
        return nodeLists.size();
    }




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

