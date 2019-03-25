package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.util.Base64Coder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@UmDao
@UmRepository
public abstract class PersonAuthDao implements BaseDao<PersonAuth> {

    private static final int KEY_LENGTH = 512;

    private static final int ITERATIONS = 10000;

    private static String SALT = "fe10fe1010";

    public static final String ENCRYPTED_PASS_PREFIX = "e:";

    public static final String PLAIN_PASS_PREFIX = "p:";

    public static final Integer UPDATE_PASSWORD_FAIL_NOT_ADMIN = 1;
    public static final Integer UPDATE_PASSWORD_FAIL_NO_ACCESS = 2;
    public static final Integer UPDATE_PASSWORD_FAIL_DB = 3;
    public static final Integer UPDATE_PASSWORD_SUCCESS = 4;


    public static String encryptPassword(String originalPassword) {
        PBEKeySpec keySpec = new PBEKeySpec(originalPassword.toCharArray(), SALT.getBytes(),
                ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return new String(Base64Coder.encode(keyFactory.generateSecret(keySpec).getEncoded()));
        }catch(NoSuchAlgorithmException|InvalidKeySpecException e) {
            //should not happen
            throw new AssertionError("Error hashing password" + e.getMessage(), e);
        }
    }

    public static boolean authenticateEncryptedPassword(String providedPassword,
                                                        String encryptedPassword) {
        return encryptPassword(providedPassword).equals(encryptedPassword);
    }

    @UmQuery("SELECT admin from Person WHERE personUid = :uid")
    public abstract boolean isPersonAdmin(long uid);

    @UmQuery("UPDATE PersonAuth set passwordHash = :passwordHash " +
            " WHERE personAuthUid = :personUid")
    public abstract void updatePasswordForPersonUid(long personUid, String passwordHash,
                                                    UmCallback<Integer> resultCallback);

    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public void resetPassword(long personUid, String password,
                              @UmRestAuthorizedUidParam long loggedInPersonUid,
                              UmCallback<Integer> resetCallback) {
        String passwordHash = encryptPassword(password);
        if(loggedInPersonUid != personUid){
            if(isPersonAdmin(loggedInPersonUid)){
                PersonAuth personAuth = new PersonAuth(personUid, passwordHash);
                PersonAuth existingPersonAuth = findByUid(personUid);
                if(existingPersonAuth == null){
                    insert(personAuth);
                }
                updatePasswordForPersonUid(personUid, passwordHash,
                        new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        if(result > 0) {
                            System.out.println("Update password success");
                            resetCallback.onSuccess(UPDATE_PASSWORD_SUCCESS);
                        }else{
                            resetCallback.onFailure(new Exception());
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        System.out.println("Update password fail");
                        resetCallback.onFailure(new Exception());
                    }
                });
            }else{
                System.out.println("Update password fail2");
                resetCallback.onFailure(new Exception());
            }
        }else{
            System.out.println("Update password fail3");
            resetCallback.onFailure(new Exception());
        }

    }


    @UmQuery("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    public  abstract void findByUidAsync(long uid, UmCallback<PersonAuth> resultObject);


    @UmUpdate
    public abstract void updateAsync(PersonAuth entity, UmCallback<Integer> resultObject);

}
