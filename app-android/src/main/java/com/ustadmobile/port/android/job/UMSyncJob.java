package com.ustadmobile.port.android.job;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;

import java.io.IOException;
import java.sql.SQLException;

import static com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid.KEY_CURRENTAUTH;

/**
 * This is using evernote's andoid-job to run, umsync process in the background.
 * Created by varuna on 8/24/2017.
 */

public class UMSyncJob extends Job {

    public static final String TAG = "umsync_job_tag";
    public static String DEFAULT_MAIN_SERVER_HOST_NAME = "umcloud1svlt";

    private User loggedInUser;

    private Node endNode;

    public void setLoggedInUser(User user){
        loggedInUser = user;
    }

    public void setEndNode(Node node){
        endNode = node;
    }

    public static final String KEY_CURRENTUSER = "app-currentuser";
    private SharedPreferences appPreferences;
    public static final String APP_PREFERENCES_NAME = "UMAPP-PREFERENCES";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        System.out.println("UMSyncJob: onRunJob()..");

        Object context = getContext();
        String loggedInUsername = null;
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);

        System.out.println("  UMSyncJob: Getting logged in username..");
        loggedInUsername = UstadMobileSystemImpl.getInstance().getActiveUser(context);

        System.out.println("  UMSyncJob: Checking Shared Pref..");
        SharedPreferences appPrefs = getAppSharedPreferences((Context)context);
        String currentUsername = appPrefs.getString(KEY_CURRENTUSER, null);

        if(currentUsername!=null && !currentUsername.isEmpty()){
            System.out.println("  UMSyncJob: Got username from shared pref: " + currentUsername );
            loggedInUsername = currentUsername;
        }

        if(loggedInUsername != null && !loggedInUsername.isEmpty()) {
            System.out.println("  UMSyncJob: Logged in username is : " + loggedInUsername);
            loggedInUser = userManager.findByUsername(context, loggedInUsername);

        }else{
            System.out.println("  !UMSyncJob: logged in username is null..!");
        }

        System.out.println("  UMSyncJob: Getting endNode (in this case, main node)..");
        try {
            endNode = nodeManager.getMainNode(DEFAULT_MAIN_SERVER_HOST_NAME, context);
            System.out.println("  UMSyncJob: Got endNode!");
        } catch (SQLException e) {
            System.out.println("  UMSyncJob: Could not get node..");
            e.printStackTrace();
        }

        String loggedInUserCred = appPrefs.getString(KEY_CURRENTAUTH, null);

        try {
            if(loggedInUser != null && endNode != null) {
                System.out.println("  UMSyncJob: Logged in user and end node is NOT null. Syncing..");
                //TODO: Test it.
                UstadMobileSystemImplSE.getInstanceSE().fireSetSyncHappeningEvent(true, getContext());
                UMSyncResult result = UMSyncEndpoint.startSync(loggedInUser, loggedInUserCred, endNode, getContext());
                if(result.getStatus() != -1){
                    UstadMobileSystemImplSE.getInstanceSE().fireSetSyncHappeningEvent(false, getContext());
                }
            }else{
                System.out.println("  !UMSyncJob: Logged in user and end node is null, skipping..!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }



    private SharedPreferences getAppSharedPreferences(Context context) {
        if(appPreferences == null) {
            appPreferences = context.getSharedPreferences(APP_PREFERENCES_NAME,
                    Context.MODE_PRIVATE);
        }
        return appPreferences;
    }

    @Override
    protected void onReschedule(int newJobId) {
        System.out.println("UMSyncJob: on Reschedule ..");
        super.onReschedule(newJobId);
        //the rescheduled job has a new ID.
    }
}
