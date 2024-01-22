package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedOutputStream;
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
import java.util.zip.ZipOutputStream;

public class FileHelper {

    public static String empty() {
        return "";
    }

    // creates a zip folder
    public static File createBackupZipFolder(Context context) {
        File backupDirectory = getBackupDirectory(context, true);
        existsOrCreate(backupDirectory);
        return new File(backupDirectory.getAbsolutePath() + ".zip");
    }

    public static File getInternalBackupDirectory(Context context) {
        return context.getFilesDir();
    }

    public static File getBackupDirectory(Context context, boolean insideDirectory) {
        return context.getExternalFilesDir(insideDirectory ? Environment.DIRECTORY_DOCUMENTS : empty());
    }

    /**
     * Create Directory if it does not exist
     */
    public static void existsOrCreate(File directory) {
        if (!directory.exists()) directory.mkdirs();
    }

    /**
     * Delete File if it exists
     */
    public static void newOrDelete(File file) {
        if (file.exists()) file.delete();
    }

    public static void writeFileTo(Context context, Uri fromUri, File toFile) {
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(fromUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(toFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] buf = new byte[1024];
        int len = 0;
        while (true) {
            try {
                assert in != null;
                if (!((len = in.read(buf)) > 0))
                    break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                assert out != null;
                out.write(buf, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            assert out != null;
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String zipFile, String location) {
        try {
            FileInputStream inputStream = new FileInputStream(zipFile);
            ZipInputStream zipStream = new ZipInputStream(inputStream);
            ZipEntry zEntry;
            while ((zEntry = zipStream.getNextEntry()) != null) {
                File f = new File(location, zEntry.getName());
                String canonicalPath = f.getCanonicalPath();
                if (!canonicalPath.startsWith(location)) {
                    throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", canonicalPath));
                }
                FileOutputStream out = new FileOutputStream(location + "/" + zEntry.getName());
                BufferedOutputStream bufferOut = new BufferedOutputStream(out);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = zipStream.read(buffer)) != -1) bufferOut.write(buffer, 0, read);
                zipStream.closeEntry();
                bufferOut.close();
                out.close();
            }
            zipStream.close();
        } catch (Exception e) {
            Log.d("Here", "Error unzipping backup");
            e.printStackTrace();
        }
    }

    public static boolean zip(Context context, ArrayList<String> files, Uri uri) {
        int BUFFER = 1024;
        try {
            ParcelFileDescriptor document = context.getContentResolver().openFileDescriptor(uri, "w");
            assert document != null;
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(document.getFileDescriptor())));
            byte[] data = new byte[BUFFER];
            for (String filePath : files) {
                File newFile = new File(filePath);
                if (newFile.exists()) {
                    FileInputStream fi = new FileInputStream(filePath);
                    ZipEntry entry = new ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = fi.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    fi.close();
                    out.closeEntry();
                }
            }
            out.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}