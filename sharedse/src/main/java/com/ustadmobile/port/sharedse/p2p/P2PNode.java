package com.ustadmobile.port.sharedse.p2p;

/**
 * Created by kileha3 on 21/02/2017.
 */

public class P2PNode {

    private String timeStamp;

    private String nodeAddress;

    private String networkSSID;

    private String networkPass;



    public P2PNode(String nodeAddress){
        this.nodeAddress=nodeAddress;
    }


    public void setNetworkSSID(String networkSSID) {
        this.networkSSID = networkSSID;
    }


    public String getNetworkPass() {
        return networkPass;
    }

    public void setNetworkPass(String networkPass) {
        this.networkPass = networkPass;
    }




    public String getNetworkSSID() {
        return networkSSID;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


    public String getTimeStamp() {
        return timeStamp;
    }




    @Override
    public boolean equals(Object object) {

        return object instanceof P2PNode && getNodeAddress().equals(this.nodeAddress);
    }
}
