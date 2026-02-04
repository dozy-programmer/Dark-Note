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
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
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

    public static File getTemporaryBackupDirectory(Context context) {
        File tempDir = new File(context.getCacheDir(), "temp_backup");
        existsOrCreate(tempDir);
        return tempDir;
    }

    public static void moveDirectory(File from, File to) throws IOException {
        if (!to.exists()) {
            to.mkdirs();
        }

        File[] files = from.listFiles();
        if (files != null) {
            for (File file : files) {
                File newFile = new File(to, file.getName());
                if (file.isDirectory()) {
                    moveDirectory(file, newFile);
                } else {
                    copyFile(file, newFile);
                    file.delete();
                }
            }
        }
        from.delete();
    }

    private static void copyFile(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
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

    public static void unzip(String zipFile, String location) throws IOException {
        try {
            File destDir = new File(location);
            String canonicalLocation = destDir.getCanonicalPath();

            FileInputStream inputStream = new FileInputStream(zipFile);
            ZipInputStream zipStream = new ZipInputStream(inputStream);
            ZipEntry zEntry;
            while ((zEntry = zipStream.getNextEntry()) != null) {
                File f = new File(destDir, zEntry.getName());
                String canonicalPath = f.getCanonicalPath();
                if (!canonicalPath.startsWith(canonicalLocation)) {
                    throw new IOException(String.format("Found Zip Path Traversal Vulnerability with %s", canonicalPath));
                }
                FileOutputStream out = new FileOutputStream(f);
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
            Log.d("Here", "Error unzipping backup", e);
            throw new IOException("Unzip failed: " + e.getMessage(), e);
        }
    }

    public static File extractFileFromZip(String zipFilePath, String fileNameToExtract, File outputFile) throws IOException {
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zEntry;
            boolean fileFound = false;
            while ((zEntry = zipStream.getNextEntry()) != null) {
                if (zEntry.getName().equals(fileNameToExtract)) {
                    fileFound = true;
                    try (FileOutputStream out = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = zipStream.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    break;
                }
            }
            if (!fileFound) {
                throw new FileNotFoundException("File not found in zip: " + fileNameToExtract);
            }
        }
        return outputFile;
    }

    public static List<String> zip(Context context, ArrayList<String> files, Uri uri) throws IOException {
        int BUFFER = 1024;
        List<String> missingFiles = new ArrayList<>();
        try (ParcelFileDescriptor document = context.getContentResolver().openFileDescriptor(uri, "w")) {
            if (document == null) {
                throw new IOException("Could not open file descriptor for URI: " + uri);
            }
            try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(document.getFileDescriptor())))) {
                byte[] data = new byte[BUFFER];
                for (String filePath : files) {
                    File newFile = new File(filePath);
                    if (!newFile.exists()) {
                        if (filePath.endsWith(".realm")) {
                            throw new FileNotFoundException("Realm file not found, aborting backup: " + filePath);
                        } else {
                            Log.w("FileHelper", "File not found, skipping: " + filePath);
                            missingFiles.add(filePath);
                            continue;
                        }
                    }
                    try (FileInputStream fi = new FileInputStream(filePath)) {
                        ZipEntry entry = new ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1));
                        out.putNextEntry(entry);
                        int count;
                        while ((count = fi.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                        out.closeEntry();
                    }
                }
            }
        }

        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd != null && !files.isEmpty() && pfd.getStatSize() == 0) {
                throw new IOException("Zip file is empty, but files were provided.");
            }
        }
        return missingFiles;
    }

}

