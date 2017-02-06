package com.ustadmobile.port.sharedse.p2p;

/**
 * Created by kileha3 on 05/02/2017.
 */

public class P2PNode {

    private String address;

    private String name;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int status;

    public P2PNode(String address) {
        this.address = address;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof P2PNode && ((P2PNode) obj).getAddress().equals(this.address);
    }
}
