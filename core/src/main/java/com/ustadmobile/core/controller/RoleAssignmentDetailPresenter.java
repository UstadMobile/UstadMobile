package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.view.RoleAssignmentDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.Role;

import static com.ustadmobile.core.view.RoleAssignmentDetailView.ENTITYROLE_UID;


/**
 * Presenter for RoleAssignmentDetail view
 **/
public class RoleAssignmentDetailPresenter extends UstadBaseController<RoleAssignmentDetailView> {

    UmAppDatabase repository;

    private long currentEntityRoleUid = 0;
    private EntityRole originalEntityRole;
    private EntityRole updatedEntityRole;

    private UmLiveData<List<PersonGroup>> groupUmLiveData;
    private UmLiveData<List<Role>> roleUmLiveData;
    private UmLiveData<List<Location>> locationUmLiveData;
    private UmLiveData<List<Clazz>> clazzUmLiveData;
    private UmLiveData<List<Person>> personUmLiveData;

    private HashMap<Long, Integer> groupIdToPosition;
    private HashMap<Integer, Long> groupPositionToId;
    private HashMap<Long, Integer> roleIdToPosition;
    private HashMap<Integer, Long> rolePositionToId;
    private HashMap<Long, Integer> locationIdToPosition;
    private HashMap<Integer, Long> locationPositionToId;
    private HashMap<Long, Integer> clazzIdToPosition;
    private HashMap<Integer, Long> clazzPositionToId;
    private HashMap<Long, Integer> peopleIdToPosition;
    private HashMap<Integer, Long> peoplePositionToId;

    private EntityRoleDao entityRoleDao;

    private PersonGroupDao groupDao;
    private RoleDao roleDao;
    private ClazzDao clazzDao;
    private LocationDao locationDao;
    private PersonDao personDao;

    private int currentTableId;

    private String[] assigneePresets, groupPresets, rolePresets, scopePresets;

    public RoleAssignmentDetailPresenter(Object context, Hashtable arguments, RoleAssignmentDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        entityRoleDao = repository.getEntityRoleDao();
        groupDao = repository.getPersonGroupDao();
        roleDao = repository.getRoleDao();
        clazzDao = repository.getClazzDao();
        locationDao = repository.getLocationDao();
        personDao = repository.getPersonDao();
        groupIdToPosition = new HashMap<>();
        groupPositionToId = new HashMap<>();
        roleIdToPosition= new HashMap<>();
        rolePositionToId = new HashMap<>();
        locationIdToPosition= new HashMap<>();
        locationPositionToId = new HashMap<>();
        clazzIdToPosition= new HashMap<>();
        clazzPositionToId = new HashMap<>();
        peopleIdToPosition= new HashMap<>();
        peoplePositionToId = new HashMap<>();

        if(arguments.containsKey(ENTITYROLE_UID)){
            currentEntityRoleUid = (long) arguments.get(ENTITYROLE_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentEntityRoleUid ==0){
            EntityRole newEntityRole = new EntityRole();
            newEntityRole.setErActive(false);
            entityRoleDao.insertAsync(newEntityRole, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromEntityRole(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else{
            initFromEntityRole(currentEntityRoleUid);
        }

    }

    public void updateAssigneePresets(int tableId){

        switch(tableId){
            case Clazz.TABLE_ID:
                clazzUmLiveData = clazzDao.findAllLive();
                clazzUmLiveData.observe(RoleAssignmentDetailPresenter.this,
                        RoleAssignmentDetailPresenter.this::handleAllClazzChanged);
                break;
            case Location.TABLE_ID:
                //TODO:
                break;
            case Person.TABLE_ID:
                personUmLiveData = personDao.findAllActiveLive();
                personUmLiveData.observe(RoleAssignmentDetailPresenter.this,
                        RoleAssignmentDetailPresenter.this::handleAllPersonChanged);
                break;
        }

    }

    public void initFromEntityRole(long uid){
        this.currentEntityRoleUid = uid;

        UmLiveData<EntityRole> entityRoleLiveData =
                entityRoleDao.findByUidLive(currentEntityRoleUid);
        entityRoleLiveData.observe(RoleAssignmentDetailPresenter.this,
                RoleAssignmentDetailPresenter.this::handleEntityRoleChanged);

        entityRoleDao.findByUidAsync(uid, new UmCallback<EntityRole>() {
            @Override
            public void onSuccess(EntityRole result) {
                updatedEntityRole = result;

                //Update group
                groupUmLiveData = groupDao.findAllActivePersonGroupsLive();
                groupUmLiveData.observe(RoleAssignmentDetailPresenter.this,
                        RoleAssignmentDetailPresenter.this::handleAllGroupsChanged);

                //Update roles
                roleUmLiveData = roleDao.findAllActiveRolesLive();
                roleUmLiveData.observe(RoleAssignmentDetailPresenter.this,
                        RoleAssignmentDetailPresenter.this::handleAllRolesChanged);

                long groupUid = updatedEntityRole.getErGroupUid();
                long roleUid = updatedEntityRole.getErRoleUid();
                int groupSelected = 0, roleSelected = 0;
                if(groupUid != 0 && roleUid != 0 && groupIdToPosition != null
                        && roleIdToPosition != null){
                    if(groupIdToPosition.containsKey(groupUid))
                        groupSelected = groupIdToPosition.get(groupUid);
                    if(roleIdToPosition.containsKey(roleUid))
                        roleSelected = roleIdToPosition.get(roleUid);
                }
                //Update scope and assignee
                view.updateRoleAssignmentOnView(updatedEntityRole, groupSelected,
                        roleSelected);

            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });
    }


    public void handleAllGroupsChanged(List<PersonGroup> groups){
        int selectedPosition = 0;

        ArrayList<String> entityList = new ArrayList<>();
        groupIdToPosition = new HashMap<>();
        int posIter = 0;
        for(PersonGroup everyEntity: groups){
            entityList.add(everyEntity.getGroupName());
            groupIdToPosition.put(everyEntity.getGroupUid(), posIter);
            groupPositionToId.put(posIter, everyEntity.getGroupUid());
            posIter++;
        }
        groupPresets = new String[entityList.size()];
        groupPresets = entityList.toArray(groupPresets);

        if(originalEntityRole == null){
            originalEntityRole = new EntityRole();
        }
        if(originalEntityRole.getErGroupUid() != 0){
            long groupUid = originalEntityRole.getErGroupUid();
            if(groupIdToPosition.containsKey(groupUid)){
                selectedPosition = groupIdToPosition.get(groupUid);
            }
        }

        view.setGroupPresets(groupPresets, selectedPosition);
    }

    public void handleAllRolesChanged(List<Role> roles){
        int selectedPosition = 0;

        ArrayList<String> entityList = new ArrayList<>();
        roleIdToPosition = new HashMap<>();
        int posIter = 0;
        for(Role everyEntity: roles){
            entityList.add(everyEntity.getRoleName());
            roleIdToPosition.put(everyEntity.getRoleUid(), posIter);
            rolePositionToId.put(posIter, everyEntity.getRoleUid());
            posIter++;
        }
        rolePresets = new String[entityList.size()];
        rolePresets = entityList.toArray(rolePresets);

        if(originalEntityRole == null){
            originalEntityRole = new EntityRole();
        }
        if(originalEntityRole.getErRoleUid() != 0){
            long entityUid = originalEntityRole.getErRoleUid();
            if(roleIdToPosition.containsKey(entityUid)){
                selectedPosition = roleIdToPosition.get(entityUid);
            }
        }

        view.setRolePresets(rolePresets, selectedPosition);
    }

    public String[] getScopePresets() {
        return scopePresets;
    }

    public void setScopePresets(String[] scopePresets) {
        this.scopePresets = scopePresets;
    }

    public void handleAllLocationsChanged(List<Location> locations){

    }

    public void handleAllClazzChanged(List<Clazz> clazzes){
        int selectedPosition = 0;

        ArrayList<String> entityList = new ArrayList<>();
        clazzIdToPosition = new HashMap<>();
        int posIter = 0;
        for(Clazz everyEntity: clazzes){
            entityList.add(everyEntity.getClazzName());
            clazzIdToPosition.put(everyEntity.getClazzUid(), posIter);
            clazzPositionToId.put(posIter, everyEntity.getClazzUid());
            posIter++;
        }
        assigneePresets = new String[entityList.size()];
        assigneePresets = entityList.toArray(assigneePresets);

        if(originalEntityRole == null){
            originalEntityRole = new EntityRole();
        }
        if(originalEntityRole.getErEntityUid() != 0){
            long entityUid = originalEntityRole.getErEntityUid();
            if(clazzIdToPosition.containsKey(entityUid)){
                selectedPosition = clazzIdToPosition.get(entityUid);
            }
        }

        view.setAssigneePresets(assigneePresets, selectedPosition);
    }

    public void handleAllPersonChanged(List<Person> people){
        int selectedPosition = 0;

        ArrayList<String> entityList = new ArrayList<>();
        peopleIdToPosition = new HashMap<>();
        int posIter = 0;
        for(Person everyEntity: people){
            entityList.add(everyEntity.getFirstNames() + " " + everyEntity.getLastName());
            peopleIdToPosition.put(everyEntity.getPersonUid(), posIter);
            peoplePositionToId.put(posIter, everyEntity.getPersonUid());
            posIter++;
        }
        assigneePresets = new String[entityList.size()];
        assigneePresets = entityList.toArray(assigneePresets);

        if(originalEntityRole == null){
            originalEntityRole = new EntityRole();
        }
        if(originalEntityRole.getErEntityUid() != 0){
            long entityUid = originalEntityRole.getErEntityUid();
            if(peopleIdToPosition.containsKey(entityUid)){
                selectedPosition = peopleIdToPosition.get(entityUid);
            }
        }

        view.setAssigneePresets(assigneePresets, selectedPosition);
    }


    public void updateGroup(int position){

        if(groupPositionToId.containsKey(position))
            updatedEntityRole.setErGroupUid(groupPositionToId.get(position));
    }
    public void updateRole(int position){
        if(rolePositionToId.containsKey(position))
        updatedEntityRole.setErRoleUid(rolePositionToId.get(position));
    }
    public void updateScope(int position){
        currentTableId = 0;
        switch (position){
            case 0:
                currentTableId=Clazz.TABLE_ID;break;
            case 1:
                currentTableId=Person.TABLE_ID;break;
            case 2:
                currentTableId=Location.TABLE_ID;break;
        }
        updatedEntityRole.setErTableId(currentTableId);
    }

    public void updateAssignee(int position){
        long entityUid = 0;
        switch (currentTableId){
            case Clazz.TABLE_ID:
                if(clazzPositionToId.containsKey(position))
                    entityUid = clazzPositionToId.get(position);
                break;
            case Person.TABLE_ID:
                if(peoplePositionToId.containsKey(position))
                    entityUid = peoplePositionToId.get(position);
                break;
            case Location.TABLE_ID:
                if(locationPositionToId.containsKey(position))
                    entityUid = locationPositionToId.get(position);
                break;
        }
        if(entityUid != 0){
            updatedEntityRole.setErEntityUid(entityUid);
        }
    }

    public void handleEntityRoleChanged(EntityRole changedEntityRole){
        //set the og person value
        if(originalEntityRole == null)
            originalEntityRole = changedEntityRole;

        if(updatedEntityRole == null || !updatedEntityRole.equals(changedEntityRole)) {
            //update class edit views

            view.updateRoleAssignmentOnView(updatedEntityRole, 0, 0);
            //Update the currently editing class object
            updatedEntityRole = changedEntityRole;
        }
    }

    public void handleClickDone() {

        view.finish();
    }
}
