package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

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

    public void handleClickClazz(Clazz clazz) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        long clazzUid = clazz.getClazzUid();
        args.put("ClazzUid", clazzUid);
        impl.go(ClassDetailView.VIEW_NAME, args, view.getContext());

    }

    public void handleClickClazzRecordAttendance(Clazz clazz) {

    }


    @Override
    public void setUIStrings() {

    }
}
