package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.lib.db.entities.Clazz;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;


/**
 * The ClazzDetail Presenter.
 */
public class ClazzDetailPresenter
        extends UstadBaseController<ClassDetailView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;


    public ClazzDetailPresenter(Object context, Hashtable arguments, ClassDetailView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();

        clazzDao.findByUidAsync(currentClazzUid, new UmCallback<Clazz>() {
            @Override
            public void onSuccess(Clazz result) {
                view.setToolbarTitle(result.getClazzName());
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println("ClazzDetailPresenter: Fail to get Clazz");
            }
        });

    }

    @Override
    public void setUIStrings() {

    }

}
