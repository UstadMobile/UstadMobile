package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.RoleDetailView;
import com.ustadmobile.core.view.RoleListView;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.UMCalendar;

import static com.ustadmobile.core.view.RoleListView.ROLE_UID;


/**
 * Presenter for RoleDetail view
 **/
public class RoleDetailPresenter extends UstadBaseController<RoleDetailView> {

    UmAppDatabase repository;
    private long currentRoleUid = 0;
    private Role currentRole;
    private Role updatedRole;
    RoleDao roleDao;

    public long getPermissionField() {
        return permissionField;
    }

    public void setPermissionField(long permissionField) {
        this.permissionField = permissionField;
    }

    private long permissionField = 0;

    public RoleDetailPresenter(Object context, Hashtable arguments, RoleDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        roleDao = repository.getRoleDao();

        if(arguments.containsKey(ROLE_UID)){
            currentRoleUid = (long) arguments.get(ROLE_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentRoleUid == 0){
            currentRole = new Role();
            currentRole.setRoleName("");
            currentRole.setRoleActive(false);
            roleDao.insertAsync(currentRole, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromRole(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else{
            initFromRole(currentRoleUid);
        }
    }

    public void handleRoleChanged(Role changedRole){

        //set the og person value
        if(currentRole == null)
            currentRole = changedRole;

        if(updatedRole == null || !updatedRole.equals(changedRole)) {
            //update class edit views
            view.updateRoleOnView(updatedRole);
            //Update the currently editing class object
            updatedRole = changedRole;
        }
    }

    public void initFromRole(long roleUid){
        this.currentRoleUid = roleUid;

        UmLiveData<Role> roleUmLiveData = roleDao.findByUidLive(currentRoleUid);
        //Observe the live data
        roleUmLiveData.observe(RoleDetailPresenter.this,
                RoleDetailPresenter.this::handleRoleChanged);


        roleDao.findByUidAsync(roleUid, new UmCallback<Role>() {
            @Override
            public void onSuccess(Role result) {
                updatedRole = result;
                view.updateRoleOnView(updatedRole);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateRoleName(String name){
        updatedRole.setRoleName(name);
    }


    public void handleClickDone() {

        updatedRole.setRoleActive(true);
        updatedRole.setRolePermissions(permissionField);

        roleDao.updateAsync(updatedRole, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }


}
