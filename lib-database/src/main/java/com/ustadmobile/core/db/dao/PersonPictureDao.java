package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.db.entities.PersonPicture;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@UmDao(hasAttachment = true,
        permissionJoin = " LEFT JOIN Person ON PersonPicture.personPicturePersonUid = Person.personUid ",
        selectPermissionCondition = PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
                Role.PERMISSION_PERSON_PICTURE_SELECT + PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION2,
        updatePermissionCondition = PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
                Role.PERMISSION_PERSON_PICTURE_UPDATE + PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION2)
@UmRepository
public abstract class PersonPictureDao implements SyncableDao<PersonPicture, PersonPictureDao> {

    public static final String TABLE_LEVEL_PERMISSION = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
            "OR " +
            "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
            " JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " WHERE " +
            " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND EntityRole.erTableId = " + PersonPicture.TABLE_ID +
            " AND Role.rolePermissions & ";

    protected static final String TABLE_LEVEL_PERMISSION_CONDITION2 = " > 0)";

    @UmDbSetAttachment
    public abstract void setAttachment(long uid, InputStream pictureAttachment) throws IOException;

    @UmDbGetAttachment
    public abstract String getAttachmentPath(long uid);

    @UmDbGetAttachment
    public abstract InputStream getAttachmentStream(long uid) throws IOException;

    @UmDbSetAttachment
    public abstract void setAttachmentFromTmpFile(long uid, File tmpFile);

    @UmQuery("SELECT * FROM PersonPicture where personPicturePersonUid = :personUid ORDER BY " +
            " picTimestamp DESC LIMIT 1")
    public abstract void findByPersonUidAsync(long personUid, UmCallback<PersonPicture> resultObject);


    @UmDbSetAttachment
    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public abstract void uploadAttachment(long uid, InputStream attachment) throws IOException;

    @UmDbGetAttachment
    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public abstract InputStream downloadAttachment(long uid) throws IOException;



}