package com.ustadmobile.port.sharedse.p2p;

/**
 * Created by kileha3 on 21/02/2017.
 */


public class P2PNode {


    private String nodeAddress;

    private String networkSSID;

    private String networkPass;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int status;



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






    @Override
    public boolean equals(Object object) {
        return object instanceof P2PNode && ((P2PNode)object).getNodeAddress().equals(this.nodeAddress);
    }
}
