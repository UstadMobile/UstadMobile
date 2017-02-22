package com.ustadmobile.port.android.p2p;

/**
 * Created by Lincoln on 18/03/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.p2p.P2PNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
send device list to the UI when it is available
*/

public class P2PListChangeReceiver extends BroadcastReceiver {

    public static NodeListChangeReceiverListener nodeListChangeReceiverListener;

    public P2PListChangeReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent arg1) {

        try{
            JSONObject nodeData=new JSONObject(UstadMobileSystemImpl.getInstance().getAppPref("devices",context));
            JSONArray nodes=nodeData.getJSONArray("devices");
            ArrayList<P2PNode> nodeList=new ArrayList<>();

            P2PNode deviceNode;

            for(int position=0;position<nodes.length();position++){

                JSONObject data=nodes.getJSONObject(position);
                deviceNode=new P2PNode(data.getString("id"));
                deviceNode.setName(data.getString("name"));
                deviceNode.setAddress(data.getString("id"));
                deviceNode.setStatus(Integer.parseInt(data.getString("status")));
                deviceNode.setNodeType(data.getString("type"));
                nodeList.add(deviceNode);
            }

            if (nodeListChangeReceiverListener != null) {
                nodeListChangeReceiverListener.onNodeListChange(nodeList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * list change implementable listener*/

    public interface NodeListChangeReceiverListener {
        void onNodeListChange(ArrayList<P2PNode> nodeList);
    }

}