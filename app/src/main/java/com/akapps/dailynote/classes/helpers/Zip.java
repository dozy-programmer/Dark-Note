package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.akapps.dailynote.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.realm.RealmList;
import io.realm.RealmResults;

public class Zip{
    private static int BUFFER_SIZE = 6 * 1024;

    // creates a zip folder
    public static String createZipFolder(Context context, String folderName){
        File storageDir = new File(context.getApplicationContext()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator +
                folderName);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return storageDir.getAbsolutePath();
    }

    // places all the photos in a zip file and returns a string of the file path
    public static String zipPhotos(Context context, ArrayList<String> files) {

        String zipFolder = createZipFolder(context, "Dark_Note_Backup");
        String zippath = zipFolder + ".zip";
        File zipFile = new File(zippath);

        int BUFFER = 1024;

        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];

            for (int i = 0; i < files.size(); i++) {
                FileInputStream fi = new FileInputStream(files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zipFile.getAbsolutePath();
    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

}
