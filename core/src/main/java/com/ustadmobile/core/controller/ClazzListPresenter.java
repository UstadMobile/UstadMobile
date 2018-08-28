package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

public class ClazzListPresenter extends UstadBaseController<ClazzListView> {

    private long currentPersonUid = 0L;

    private UmProvider<ClazzWithNumStudents> clazzListProvider;

    public ClazzListPresenter(Object context, Hashtable arguments, ClazzListView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                .findAllClazzesByPersonUid(currentPersonUid);
        view.setClazzListProvider(clazzListProvider);
    }

    /**
     * Click class card handler
     *
     * @param clazz
     */
    public void handleClickClazz(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        long clazzUid = clazz.getClazzUid();
        args.put("ClazzUid", clazzUid);
        impl.go(ClassDetailView.VIEW_NAME, args, view.getContext());

    }

    /**
     * Click attendance button in Class card handler
     *
     * @param clazz
     */
    public void handleClickClazzRecordAttendance(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        long clazzUid = clazz.getClazzUid();
        args.put("clazzuid", clazzUid);



        args.put("logdate", System.currentTimeMillis());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());


        /*
        ClazzLogDao clazzLogDao = UmAppDatabase.getInstance(view.getContext()).getClazzLogDao();
        ClazzLog newClazzLog = new ClazzLog();
        newClazzLog.setClazzClazzUid(clazzUid);
        newClazzLog.setDone(false);
        newClazzLog.setLogDate(System.currentTimeMillis());
        clazzLogDao.insertAsync(newClazzLog, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                newClazzLog.setClazzLogUid(result);
                args.put("clazzloguid", result);
                impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
        */


    }


    @Override
    public void setUIStrings() {

    }
}
