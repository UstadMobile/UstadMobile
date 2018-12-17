package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.PersonAuth;
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


}
