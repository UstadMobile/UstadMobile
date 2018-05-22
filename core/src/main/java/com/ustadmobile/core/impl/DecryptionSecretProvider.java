package com.ustadmobile.core.impl;

/**
 * Created by mike on 2/17/18.
 */

public interface DecryptionSecretProvider {

    DecryptionSecret getSecret(String uri);

}
