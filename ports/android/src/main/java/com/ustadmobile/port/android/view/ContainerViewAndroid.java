package com.ustadmobile.port.android.view;

import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import android.support.v4.app.Fragment;


import java.util.*;

/**
 * Created by mike on 8/14/15.
 */
public class ContainerViewAndroid {

    private static Map<Integer, ContainerViewAndroid> viewMap;

    private static int idCounter = 0;

    private int viewId;

    private ContainerController containerController;

    private ContainerViewAndroid containerView;

    private ContainerActivity containerActivity;

    private String title;

    static {
        viewMap = new HashMap<Integer, ContainerViewAndroid>();
    }

    public ContainerViewAndroid() {
        viewId = ContainerViewAndroid.idCounter;
        ContainerViewAndroid.idCounter++;
        viewMap.put(new Integer(viewId), this);
    }



    public static ContainerViewAndroid getViewById(int viewId) {
        return viewMap.get(new Integer(viewId));
    }

    public void setContainerActivity(ContainerActivity containerActivity) {
        this.containerActivity = containerActivity;
    }

    public int getViewId() {
        return viewId;
    }


    public ContainerController getContainerController() {
        return containerController;
    }



}
