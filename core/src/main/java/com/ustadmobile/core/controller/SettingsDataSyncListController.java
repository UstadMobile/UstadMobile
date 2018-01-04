package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SettingsDataSyncListView;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.NodeSyncStatusManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.NodeSyncStatus;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kileha3 on 13/02/2017.
 */

public class SettingsDataSyncListController extends UstadBaseController{

    public static String DEFAULT_MAIN_SERVER_HOST_NAME = "umcloud1svlt";

    private SettingsDataSyncListView view = null;

    public SettingsDataSyncListController(Object context) {
        super(context);
    }

    public void setView(UstadView view) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        super.setView(view);
        this.view = (SettingsDataSyncListView)view;
    }

    /**
     * Get list of syncs
     * @param node
     * @param context
     * @return
     */
    public LinkedHashMap<String, String> getSyncHistory(Node node, Object context){
        NodeSyncStatusManager nodeSyncStatusManager =
                PersistenceManager.getInstance().getManager(NodeSyncStatusManager.class);
        LinkedHashMap syncHistoryMap = new LinkedHashMap<>();

        try {
            List<NodeSyncStatus> allStatuses = nodeSyncStatusManager.getStatusesByNode(context, node);
            Iterator<NodeSyncStatus> allStatusesIterator = allStatuses.iterator();
            while(allStatusesIterator.hasNext()){
                NodeSyncStatus thisNodeStatus = allStatusesIterator.next();
                String thisNodeStatusResult = "SUCCESS";
                if(!thisNodeStatus.getSyncResult().equals("200")){
                    thisNodeStatusResult = "FAIL";
                }
                syncHistoryMap.put(thisNodeStatus.getSyncDate(), thisNodeStatusResult);
            }
            return syncHistoryMap;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LinkedHashMap<String, String> getMainNodeSyncHistory(Object context){
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        Node mainNode = null;
        try {
            mainNode = nodeManager.getMainNode(DEFAULT_MAIN_SERVER_HOST_NAME, context);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return getSyncHistory(mainNode, context);
    }

    public void setUIStrings() {
    }

}
