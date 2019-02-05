package com.ustadmobile.core.xlsx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip util
 */
public class ZipUtil {
    private static final int BUFFER = 2048;


    /**
     * Creates an empty Zip file.
     * @param zipPath       The path of the zip.
     * @return              The zip file as File object. Null if failed.
     * @throws IOException  File exception
     */
    public static File createEmptyZipFile(String zipPath) throws IOException {

        //Create the file.
        File output = new File(zipPath);
        if(output.createNewFile()) {
            createZip(zipPath);
            return new File(zipPath);
        }else{
            return null;
        }
    }


    public static void createZip(String outfile){

        try{
            //input file
            FileInputStream input = new FileInputStream(outfile);
            //output file
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outfile));
            //name the file inside the zip file
            zip.putNextEntry(new ZipEntry(outfile));

            byte[] buffer = new byte[1024];
            int len;
            //copy the file to the zip
            while((len= input.read(buffer)) > 0){
                System.out.println(len);
                zip.write(buffer, 0, len);
            }
            zip.closeEntry();
            zip.flush();
            input.close();
            zip.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public boolean zipThisFoldersContents(String sourcePath, String toLocation){
        File folder = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));

            zipSubFolder(out, folder, sourcePath.length());

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * Zips soure path folder to location
     * @param sourcePath    Source of folder to zip
     * @param toLocation    Zip file path to save to.
     * @return
     */
    public boolean zipFileAtPath(String sourcePath, String toLocation) {

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Zips a subfolder
     *
     * @param out               Zip outputstream
     * @param folder            The subfolder
     * @param basePathLength    The basePath length
     * @throws IOException      exception
     */
    public void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        File[] fileList = folder.listFiles();
        BufferedInputStream origin;
        for (File currentFile : fileList) {
            if (currentFile.isDirectory()) {
                zipSubFolder(out, currentFile, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String currentFilePath = currentFile.getPath();
                String relativePath = currentFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(currentFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(currentFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /**
     * Gets the last path component
     * @param filePath  the file path
     * @return  The last path
     */
    private String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        return segments[segments.length - 1];
    }

    /**
     * Adds a list of files to a zip
     * @param files     A string array of all files paths that need to be added to the zip
     * @param zipFile   The file path of the zip file to add the files to.
     */
    public static void zip(String[] files, String zipFile) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < files.length; i++) {
                System.out.print("Adding: " + files[i]);
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.finish();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}