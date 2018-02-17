package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 2/17/18.
 */

public class ZipContentTypePluginHelper {

    public static ZipFile openAndUnlock(File file) throws IOException {
        ZipFile epubZip = null;
        try {
            epubZip = new ZipFile(file);
            if (epubZip.isEncrypted()) {
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                if (impl.getDecryptionSecretProvider() != null) {
                    epubZip.setPassword(impl.getDecryptionSecretProvider()
                            .getSecret(file.getAbsolutePath()).getAsCharArray());
                } else {
                    throw new IOException("Encrypted file : no decryption provider");
                }
            }
        } catch (ZipException|IOException e) {
            e.printStackTrace();
        }

        return epubZip;
    }


}
