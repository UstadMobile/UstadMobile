package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.core.db.UmLiveData;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;


/**
 * The ClazzEdit Presenter.
 */
public class ClazzEditPresenter
        extends UstadBaseController<ClazzEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private Clazz mOriginalClazz;
    private Clazz mUpdatedClazz;

    private UmLiveData<Clazz> clazzLiveData;


    public ClazzEditPresenter(Object context, Hashtable arguments, ClazzEditView view) {
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
        //Get person live data and observe
        clazzLiveData = clazzDao.findByUidLive(currentClazzUid);
        //Observe the live data
        clazzLiveData.observe(ClazzEditPresenter.this,
                ClazzEditPresenter.this::handleClazzValueChanged);


    }

    public void handleClazzValueChanged(Clazz clazz){
        //TODO

        //set the og person value
        if(mOriginalClazz == null)
            mOriginalClazz = clazz;

        if(mUpdatedClazz == null || !mUpdatedClazz.equals(clazz)) {

            //updateClazzViews

            mUpdatedClazz = clazz;
        }
    }

    public void handleClickPrimaryActionButton(long selectedObjectUid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments
        Hashtable args = new Hashtable();
        //eg: args.put(ARG_CLAZZ_UID, selectedObjectUid);

        //Go to view
        //eg: impl.go(SELEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }

}
