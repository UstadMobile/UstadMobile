package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 21/02/2017.
 */


public class NetworkNode {


    private String nodeMacAddress;

    private String networkSSID;

    private String networkPass;

    private String nodeIPAddress;

    private String nodeBluetoothAddress;

    private int nodePortNumber;

    private int status;
    private boolean locallyFound=false;



    public NetworkNode(String nodeMacAddress){
        locallyFound=false;
        this.nodeMacAddress = nodeMacAddress;
    }

    public NetworkNode(String nodeIPAddress, int portNumber){
        locallyFound=true;
        this.nodeIPAddress=nodeIPAddress;
        this.nodePortNumber=portNumber;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getNodeBluetoothAddress() {
        return nodeBluetoothAddress;
    }

    public void setNodeBluetoothAddress(String nodeBluetoothAddress) {
        this.nodeBluetoothAddress = nodeBluetoothAddress;
    }

    public void setNodePortNumber(int nodePortNumber) {
        this.nodePortNumber = nodePortNumber;
    }

    @Override
    public boolean equals(Object object) {
        if(locallyFound){
            return object instanceof NetworkNode && ((NetworkNode)object).getNodeIPAddress().equals(this.nodeIPAddress)
                    && ((NetworkNode)object).getNodePortNumber()==this.nodePortNumber;
        }else{
            return object instanceof NetworkNode && ((NetworkNode)object).getNodeMacAddress().equals(this.nodeMacAddress);
        }
    }
}
