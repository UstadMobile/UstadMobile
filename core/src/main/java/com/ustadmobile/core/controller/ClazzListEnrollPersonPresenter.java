package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.view.ClazzListEnrollPersonView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.util.Hashtable;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

public class ClazzListEnrollPersonPresenter extends UstadBaseController<ClazzListEnrollPersonView> {

    private long currentPersonUid = -1L;

    private UmProvider<ClazzWithNumStudents> clazzListProvider;

    public ClazzListEnrollPersonPresenter(Object context,
                                          Hashtable arguments, ClazzListEnrollPersonView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = Long.parseLong(arguments.get(ARG_PERSON_UID).toString());
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate clazzes
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();
        clazzListProvider = clazzDao.findAllClazzesByPersonUid(currentPersonUid);
        view.setClazzListProvider(clazzListProvider);

    }

    public void handleClickDone(){
        view.finish();
    }

    public void handleClickClazz(Clazz clazzSelected){

    }

    @Override
    public void setUIStrings() {

    }
}
