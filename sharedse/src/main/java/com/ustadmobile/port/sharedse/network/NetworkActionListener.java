package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 05/02/2017.
 */

public interface NetworkActionListener {
    void onSuccess();
    void onFailure(int errorCode);
}
