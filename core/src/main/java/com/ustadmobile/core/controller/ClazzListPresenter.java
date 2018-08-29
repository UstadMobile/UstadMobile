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

    private long currentPersonUid = -1L;

    private UmProvider<ClazzWithNumStudents> clazzListProvider;

    public ClazzListPresenter(Object context, Hashtable arguments, ClazzListView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //TODO: Remove. Replace with Logged-In User
        currentPersonUid = 1L;

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

    }


    @Override
    public void setUIStrings() {

    }
}
