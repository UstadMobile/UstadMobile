package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ClazzLogDetailPresenter.ARG_LOGDATE;

public class ClazzListPresenter extends UstadBaseController<ClazzListView> {

    private long currentPersonUid = -1L;

    public static final String ARG_CLAZZ_UID = "ClazzUid";

    private UmProvider<ClazzWithNumStudents> clazzListProvider;

    public ClazzListPresenter(Object context, Hashtable arguments, ClazzListView view) {
        super(context, arguments, view);
    }

    /**
     * The ClazzListPresenter does the following:
     * 1. Populates the clazzListProvider and sets it to the view.
     *
     * @param savedState The state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //TODO: Remove. Replace with Logged-In User
        currentPersonUid = 1L;

//        clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
//                .findAllClazzesByPersonUid(currentPersonUid);
        clazzListProvider = UmAppDatabase.getInstance(context).getClazzDao()
                .findAllClazzes();
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
        args.put(ARG_CLAZZ_UID, clazzUid);
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
        args.put(ARG_CLAZZ_UID, clazzUid);
        args.put(ARG_LOGDATE, System.currentTimeMillis());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }


    public void handleClickPrimaryActionButton(){
        //Goes to ClazzEditActivity with currentClazzUid passed as argument

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Clazz  newClazz = new Clazz();
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();


        clazzDao.insertAsync(newClazz, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Hashtable args = new Hashtable();
                args.put(ARG_CLAZZ_UID, result);
                impl.go(ClazzEditView.VIEW_NAME, args, view.getContext());
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });



    }

    @Override
    public void setUIStrings() {

    }
}
