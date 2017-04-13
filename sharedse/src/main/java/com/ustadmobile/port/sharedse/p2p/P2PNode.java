package com.ustadmobile.port.sharedse.p2p;

/**
 * Created by kileha3 on 21/02/2017.
 */


public class P2PNode {


    private String nodeMacAddress;

    private String networkSSID;

    private String networkPass;

    private String nodeIPAddress;

    private int nodePortNumber;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int status;
    private boolean locallyFound=false;



    public P2PNode(String nodeMacAddress){
        locallyFound=false;
        this.nodeMacAddress = nodeMacAddress;
    }

    public P2PNode(String nodeIPAddress,int portNumber){
        locallyFound=true;
        this.nodeIPAddress=nodeIPAddress;
        this.nodePortNumber=portNumber;
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

    public String getNodeMacAddress() {
        return nodeMacAddress;
    }

    public void setNodeMacAddress(String nodeMacAddress) {
        this.nodeMacAddress = nodeMacAddress;
    }


    public String getNodeIPAddress() {
        return nodeIPAddress;
    }

    public void setNodeIPAddress(String nodeIPAddress) {
        this.nodeIPAddress = nodeIPAddress;
    }

    public int getNodePortNumber() {
        return nodePortNumber;
    }

    public void setNodePortNumber(int nodePortNumber) {
        this.nodePortNumber = nodePortNumber;
    }

    @Override
    public boolean equals(Object object) {
        if(locallyFound){
            return object instanceof P2PNode && ((P2PNode)object).getNodeIPAddress().equals(this.nodeIPAddress)
                    && ((P2PNode)object).getNodePortNumber()==this.nodePortNumber;
        }else{
            return object instanceof P2PNode && ((P2PNode)object).getNodeMacAddress().equals(this.nodeMacAddress);
        }
    }
}
