package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;

import java.util.Hashtable;

/**
 * The Presenter/Controller for ClazzLogListFragment. This is responsible in creating the provider
 * from the Dao and assigning it to the View. Any click handlers are also here.
 */
public class ClazzLogListPresenter extends UstadBaseController<ClassLogListView>{

    private long currentClazzUid = -1L;

    /**
     * Constructor to the ClazzLogList Presenter.
     * We got the class uid from the arguments passed to it.
     *
     * @param context
     * @param arguments
     * @param view
     */
    public ClazzLogListPresenter(Object context, Hashtable arguments, ClassLogListView view) {
        super(context, arguments, view);

        if(arguments.containsKey("clazzUid")){
            currentClazzUid = (long) arguments.get("clazzUid");
        }
    }

    /**
     * Presenter's setUiStrings method that doesn't do anything here.
     */
    @Override
    public void setUIStrings() {

    }

    private UmProvider<ClazzLog> clazzLogListProvider;

    /**
     * The Presenter here's onCreate(). This populates the provider and sets it to the View.
     *
     * This will be called when the implementation's View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        clazzLogListProvider = UmAppDatabase.getInstance(context).getClazzLogDao()
                .findByClazzUid(currentClazzUid);
        view.setClazzLogListProvider(clazzLogListProvider);

    }


    /**
     * Method logic to go to the Log Detail activity.
     * @param clazzLog
     */
    public void goToClazzLogDetailActivity(ClazzLog clazzLog){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put("clazzuid", clazzLog.getClazzClazzUid());
        args.put("logdate", clazzLog.getLogDate());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Method logic for what happens when we change the order of the log list
     *
     * @param order The order flag. 1 - Attendance, 2 - Date
     */
    public void handleChangeSortOrder(int order){
        //TODO: this
    }


}
