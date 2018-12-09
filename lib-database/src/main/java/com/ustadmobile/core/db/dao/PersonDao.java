package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class PersonDao implements SyncableDao<Person, PersonDao> {

    @Override
    @UmInsert
    public abstract long insert(Person entity);

    @UmInsert
    @Override
    public abstract void insertAsync(Person entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract Person findByUid(long uid);

    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Person> callback);

    @UmQuery("SELECT * FROM Person WHERE username = :username")
    public abstract void findByUsername(String username, UmCallback<Person> callback);

    @UmUpdate
    public abstract void updateAccountAsync(Person person, UmCallback<Integer> callback);

    /**
     * Determine of the given authentication is valid
     * @param username
     * @param password
     * @param callback
     */
    public void authenticate(String username, String password, UmCallback<UmAccount> callback){
        findByUsername(username, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person result) {
                if(result != null && password.equals(result.getPasswordHash()))
                    UmCallbackUtil.onSuccessIfNotNull(callback, new UmAccount(result.getPersonUid(),
                            result.getUsername(), "", ""));
                else
                    UmCallbackUtil.onSuccessIfNotNull(callback, null);
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });
    }

    public void createNewAccount(Person newAccount, UmCallback<UmAccount> callback) {
        insertAsync(newAccount, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                UmCallbackUtil.onSuccessIfNotNull(callback, new UmAccount(result, newAccount.getUsername(),
                        "", ""));
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });
    }
}
