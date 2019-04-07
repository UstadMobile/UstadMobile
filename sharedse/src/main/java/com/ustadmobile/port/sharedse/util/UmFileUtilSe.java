package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.container.ContainerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipFile;

public class UmFileUtilSe {

    /**
     * Check if the directory is writable
     * @param dir Directory to be checked
     * @return True if is writable otherwise is read only
     */
    public static boolean canWriteFileInDir(File dir) {
        boolean canWriteFiles = false;
        File testFile = new File(dir.getAbsoluteFile(),System.currentTimeMillis()+".txt");
        try{
            FileWriter writer = new FileWriter(testFile);
            writer.append("sampletest");
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            canWriteFiles = false;
        } catch (IOException e) {
            e.printStackTrace();
            canWriteFiles = false;
        }

        if(testFile.exists()){
            canWriteFiles = testFile.delete();
        }
        return canWriteFiles;
    }

    public static boolean deleteRecursively(File file){
        boolean allDeleted = true;
        for(File childFile : file.listFiles()){
            if(childFile.isDirectory()) {
                allDeleted &= deleteRecursively(childFile);
            }else if(!childFile.delete()) {
                UstadMobileSystemImpl.l(UMLog.WARN, 53, "WARN: delete recursively " +
                        "could not delete child file " + childFile.getAbsolutePath());
                childFile.deleteOnExit();
                allDeleted = false;
            }
        }

        boolean thisFileDeleted = file.delete();
        allDeleted &= thisFileDeleted;
        if(!thisFileDeleted) {
            file.deleteOnExit();
            UstadMobileSystemImpl.l(UMLog.WARN, 53, "WARN: delete recursively " +
                    "could not delete " + file.getAbsolutePath());
        }

        return allDeleted;
    }


    public static void copyFile(File src, File dst, int bufferSize) throws IOException{
        try (
            FileInputStream fin = new FileInputStream(src);
            FileOutputStream fout = new FileOutputStream(dst);
        ) {
            UMIOUtils.readFully(fin, fout, bufferSize);
        }
    }

    public static void copyFile(File src, File dst) throws IOException{
        copyFile(src, dst, UMIOUtils.DEFAULT_BUFFER_SIZE);
    }


    public static byte[] getMd5Sum(InputStream in, byte[] buf) throws IOException{
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                digest.update(buf, 0, bytesRead);
            }

            return digest.digest();
        }catch(NoSuchAlgorithmException ne) {
            throw new IOException(ne);
        }finally {
            in.close();
        }
    }

    public static byte[] getMd5Sum(InputStream in) throws IOException{
        return getMd5Sum(in, new byte[UMIOUtils.DEFAULT_BUFFER_SIZE]);
    }

    public static byte[] getMd5Sum(File file, byte[] buf) throws IOException {
        try(
            FileInputStream fin = new FileInputStream(file);
        ) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int bytesRead;
            while ((bytesRead = fin.read(buf)) != -1) {
                digest.update(buf, 0, bytesRead);
            }

            return digest.digest();
        }catch(IOException e) {
            throw e;
        }catch(NoSuchAlgorithmException ne) {
            throw new IOException(ne);
        }
    }

    public static byte[] getMd5Sum(File file) throws IOException {
        return getMd5Sum(file, new byte[UMIOUtils.DEFAULT_BUFFER_SIZE]);
    }

    public static void extractResourceToFile(String resourcePath, File destFile) throws IOException {
        try(
                FileOutputStream fout = new FileOutputStream(destFile);
                InputStream resIn = UmFileUtilSe.class.getResourceAsStream(resourcePath);
        ) {
            UMIOUtils.readFully(resIn, fout);
        }
    }

    public static File makeTempDir(String prefix, String postfix) throws IOException{
        File tmpDir = File.createTempFile(prefix, postfix);
        if(tmpDir.delete() && tmpDir.mkdirs())
            return tmpDir;
        else
            throw new IOException("Could not delete / create tmp dir");
    }

    /**
     * Represents a temporary container created from a zip
     */
    public static class TempZipContainer {

        private Container container;

        private ContainerManager containerManager;

        private File containerFileDir;

        public TempZipContainer(Container container, ContainerManager containerManager, File containerFileDir) {
            this.container = container;
            this.containerManager = containerManager;
            this.containerFileDir = containerFileDir;
        }

        public Container getContainer() {
            return container;
        }

        public ContainerManager getContainerManager() {
            return containerManager;
        }


        public File getContainerFileDir() {
            return containerFileDir;
        }

    }

    /**
     * Small helper method, mostly for use with tests. Given a path of a resource, the resource
     * will be extracted, and a container will be made. Once the contents of the zip have been
     * added to the container, the zip itself will be deleted from the disk. After usage, only
     * containerFileDir will need to be deleted. This can be helpful if you want to quickly create
     * a container for an zip based content (e.g. EPUB, Xapi, zip, etc).
     *
     * @param db UmAppDatabase
     * @param repo repo for UmAppDatabase
     * @param resourcePath The path to the resource as it stored e.g. /com/ustadmobile/path/to/file.zip
     * @param containerFileDir The temporary directory where container file entries will actually be stored
     * @return TempZipContainer representing a zip container, and info about it's entries.
     * @throws IOException if there is an IOException in the underlying operation
     */
    public static TempZipContainer makeTempContainerFromClassResource(UmAppDatabase db,
                                                                      UmAppDatabase repo,
                                                                      String resourcePath,
                                                                      File containerFileDir)  throws IOException{
        ZipFile zipFile = null;
        File tmpZipFile = null;
        try {
            tmpZipFile = File.createTempFile("makeTempContainerFromClass", "."+System.currentTimeMillis());
            extractResourceToFile(resourcePath, tmpZipFile);
            Container container = new Container();
            container.setContainerUid(repo.getContainerDao().insert(container));

            ContainerManager containerManager = new ContainerManager(container, db, repo,
                    containerFileDir.getAbsolutePath());

            zipFile = new ZipFile(tmpZipFile);
            containerManager.addEntriesFromZip(zipFile, ContainerManager.OPTION_COPY);
            return new TempZipContainer(container, containerManager, containerFileDir);
        }catch(IOException e) {
            throw e;
        }finally {
            if(zipFile != null){
                zipFile.close();
            }

            if(tmpZipFile != null && !tmpZipFile.delete())
                tmpZipFile.deleteOnExit();
        }

    }


    /**
     * Synanomous to makeTempContainerFromClassResource(db, repo, resourcePath, makeTempDir)
     *
     * @param db UmAppDatabase
     * @param repo repo for UmAppDatabase
     * @param resourcePath The path to the resource as it stored e.g. /com/ustadmobile/path/to/file.zip
     *
     * @return TempZipContainer given the above parameters
     * @throws IOException If there is an IOException in the underlying operation
     */
    public static TempZipContainer makeTempContainerFromClassResource(UmAppDatabase db,
                                                                      UmAppDatabase repo,
                                                                      String resourcePath) throws IOException{
        return makeTempContainerFromClassResource(db, repo, resourcePath,
                makeTempDir("makeTempContainerDir", "." + System.currentTimeMillis()));
    }

}
