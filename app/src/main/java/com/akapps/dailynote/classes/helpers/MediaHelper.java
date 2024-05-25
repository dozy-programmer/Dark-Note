package com.akapps.dailynote.classes.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.akapps.dailynote.classes.other.GenericInfoSheet;

import java.io.File;

public class MediaHelper {

    private final static String FILE_PROVIDER_AUTHORITY = "com.akapps.dailynote.fileprovider";

    public static void openMedia(ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia){
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE) .build());
    }

    public static String openCamera(FragmentActivity activity, Context context, ActivityResultLauncher<Uri> launcher) {
        String tempCameraPhotoPath = "";

            // Create the temporary File where the photo should go
            File photoFile = Helper.createFile(activity, "image", ".png");

            // Get the path of the temporary file
            tempCameraPhotoPath = photoFile.getAbsolutePath();

            // Get the content URI for the image file
            Uri photoURI = FileProvider.getUriForFile(activity,
                    FILE_PROVIDER_AUTHORITY,
                    photoFile);

            // Launch the camera activity
            launcher.launch(photoURI);

            return tempCameraPhotoPath;
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasMediaPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API level 33 (Android 13+)
            return hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            // All other API levels
            return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public static String[] getCameraPermission(){
        return new String[]{ Manifest.permission.CAMERA };
    }

    public static String[] getMediaPermissions(){
        final String[] MEDIA_PERMISSIONS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API level 33 (Android 13+)
            MEDIA_PERMISSIONS = new String[]{ Manifest.permission.READ_MEDIA_IMAGES };
        } else {
            // All other API levels
            MEDIA_PERMISSIONS = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };
        }

        return MEDIA_PERMISSIONS;
    }

}
