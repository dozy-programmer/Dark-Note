package com.akapps.dailynote.classes.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.akapps.dailynote.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.realm.Realm;
import kotlin.io.FilesKt;

public class RealmBackupRestore {

    private String IMPORT_REALM_FILE_NAME = "default.realm";
    private Activity activity;
    private Realm realm;
    private Context context;
    private String backupPath;

    public RealmBackupRestore(Activity activity) {
        this.activity = activity;
    }

    public void update(Activity activity, Context context) {
        this.realm = getRealm();
        this.activity = activity;
        this.context = context;
    }

    private Realm getRealm(){
        realm = Realm.getDefaultInstance();
        return realm;
    }

    public File backup_Share() {
        Context context = activity.getApplicationContext();
        File storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        + "/" + context.getString(R.string.app_name));
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File exportRealmFile = new File(storageDir, IMPORT_REALM_FILE_NAME);

        // if backup file already exists, delete it
        if(exportRealmFile.exists())
            exportRealmFile.delete();

        // copy current realm to backup file
        realm.writeCopyTo(exportRealmFile);

        realm.close();
        return exportRealmFile;
    }

    public void restore(Uri uri, String exportFileName, boolean unZip) {
        File storageDir;
        if(unZip){
            storageDir = new File(
                    activity.getApplicationContext()
                            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                            + "/" + activity.getApplicationContext()
                            .getString(R.string.app_name));
        }
        else{
            storageDir = new File(
                    activity.getApplicationContext()
                            .getExternalFilesDir(null) + "");
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File exportRealmFile = new File(storageDir, exportFileName);

        // if backup file already exists, delete it
        if(exportRealmFile.exists())
            exportRealmFile.delete();

        InputStream in = null;
        try {
            in = activity.getApplicationContext()
                    .getContentResolver().openInputStream(uri);
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        OutputStream out = null;
        try {
            out = new FileOutputStream(exportRealmFile);
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        byte[] buf = new byte[1024];
        int len = 0;
        while(true){
            try{ if (!((len=in.read(buf))>0)) break; }
            catch (IOException e) { e.printStackTrace();}
            try { out.write(buf,0,len); }
            catch (IOException e) { e.printStackTrace(); }
        }
        try { out.close(); }
        catch (IOException e) { e.printStackTrace(); }
        try { in.close(); }
        catch (IOException e) { e.printStackTrace(); }

        // Restore
        String restoreFilePath = exportRealmFile.getAbsolutePath();

        if(unZip) {
            try {
                unzip(restoreFilePath, storageDir.getPath());
                // delete imported zip file since it has been backed up
                FilesKt.deleteRecursively(new File(restoreFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        copyBundledRealmFile(restoreFilePath, exportFileName);
    }

    public String copyBundledRealmFile(String oldFilePath, String outFileName) {
        try {
            File file = new File(activity.getApplicationContext().getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void unzip(String zipFile, String location) throws IOException {
        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + File.separator + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path, false);

                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        } finally {
                            fout.close();
                        }
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> findPaths(){
        ArrayList<String> images = new ArrayList<>();
        String path = activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                + "/" + activity.getApplicationContext().getString(R.string.app_name);
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].getName().contains(".png"))
                images.add(files[i].getPath());
            else if(files[i].getName().contains(".realm"))
                backupPath = files[i].getPath();
        }

        return images;
    }

    public ArrayList<String> getImagesPath(){
        return findPaths();
    }

    public String getBackupPath(){
        return backupPath;
    }
}