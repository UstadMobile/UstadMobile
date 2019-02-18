package com.ustadmobile.port.sharedse.networkmanager;

import java.util.HashMap;

public class DeleteTaskRunner implements Runnable {

    protected long downloadSetUid;

    protected HashMap<String,String> map;

    public DeleteTaskRunner(long downloadSetUid,HashMap<String,String> map){
        this.downloadSetUid = downloadSetUid;
        this.map = map;
    }


    @Override
    public void run() {

    }
}
