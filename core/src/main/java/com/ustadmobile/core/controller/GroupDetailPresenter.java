package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.GroupDetailView;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;

import static com.ustadmobile.core.view.GroupDetailView.GROUP_UID;

/**
 * Presenter for GroupDetail view
 **/
public class GroupDetailPresenter extends UstadBaseController<GroupDetailView> {

    private UmProvider<PersonWithEnrollment> umProvider;
    UmAppDatabase repository;
    private PersonGroupMemberDao providerDao;
    private long currentGroupUid = 0;
    private PersonGroup currentGroup;
    private PersonGroup updatedGroup;
    PersonGroupDao groupDao;


    public GroupDetailPresenter(Object context, Hashtable arguments, GroupDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        providerDao = repository.getPersonGroupMemberDao();

        groupDao = repository.getPersonGroupDao();
        //Get or create group Uid

        if(arguments.contains(GROUP_UID)){
            currentGroupUid = (long) arguments.get(GROUP_UID);
        }


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentGroupUid == 0){
            currentGroup = new PersonGroup();
            currentGroup.setGroupName("");
            currentGroup.setGroupActive(false);
            groupDao.insertAsync(currentGroup, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromGroup(result);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }else{
            initFromGroup(currentGroupUid);
        }


    }

    public void initFromGroup(long groupUid){
        this.currentGroupUid = groupUid;

        UmLiveData<PersonGroup> groupUmLiveData = groupDao.findByUidLive(currentGroupUid);
        groupUmLiveData.observe(GroupDetailPresenter.this,
                GroupDetailPresenter.this::handleGroupChanged);

        groupDao.findByUidAsync(groupUid, new UmCallback<PersonGroup>() {
            @Override
            public void onSuccess(PersonGroup result) {
                updatedGroup = result;
                view.updateGroupOnView(updatedGroup);

                //Get provider
                umProvider = providerDao.findAllPersonWithEnrollmentWithGroupUid(currentGroupUid);
                view.setListProvider(umProvider);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    public void handleGroupChanged(PersonGroup changedGroup){
        if(changedGroup == null){
            currentGroup = changedGroup;
        }

        if(updatedGroup == null || updatedGroup.equals(changedGroup)){
            view.updateGroupOnView(updatedGroup);
            updatedGroup = changedGroup;
        }
    }

    public void updateGroupName(String name){
        updatedGroup.setGroupName(name);
    }

    public void handleClickDone() {

        updatedGroup.setGroupActive(true);
        groupDao.updateAsync(updatedGroup, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }

    public void handleClickAddStudent(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        //TODO: Go to some place to assign new students.
    }

    public void handleClickStudent(long uid){
        //TODO:
    }

    public void handleDeleteMember(long uid){
        providerDao.inactivateMemberFromGroupAsync(uid, currentGroupUid, null);
    }
}
