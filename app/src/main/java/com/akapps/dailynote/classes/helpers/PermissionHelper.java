package com.akapps.dailynote.classes.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.akapps.dailynote.classes.other.GenericInfoSheet;

import java.util.Map;

public class PermissionHelper {

    /**
     * Media permissions
     */
    public static String[] getMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    /**
     * Camera permissions
     */
    public static String[] getCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

    /**
     * Check if all permissions are granted
     */
    private static boolean arePermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    /**
     * Show GenericInfoSheet for denied permissions
     */
    private static void showPermissionInfo(FragmentActivity activity, String title, String message) {
        GenericInfoSheet infoSheet = new GenericInfoSheet(title, message, "Proceed", 1);
        infoSheet.show(activity.getSupportFragmentManager(), infoSheet.getTag());
    }

    private static void showPermissionInfo(Fragment fragment, String title, String message) {
        GenericInfoSheet infoSheet = new GenericInfoSheet(title, message, "Proceed", 1);
        infoSheet.show(fragment.getParentFragmentManager(), infoSheet.getTag());
    }

    /**
     * --- UNIVERSAL METHODS ---
     */

    public static void checkAndRequestPermissions(Fragment fragment,
                                                  String[] permissions,
                                                  String title,
                                                  String message,
                                                  Runnable onGranted) {

        if (arePermissionsGranted(fragment.requireContext(), permissions)) {
            onGranted.run();
            return;
        }

        ActivityResultLauncher<String[]> launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        onGranted.run();
                    } else {
                        showPermissionInfo(fragment, title, message);
                    }
                }
        );

        launcher.launch(permissions);
    }

    public static void checkAndRequestPermissions(FragmentActivity activity,
                                                  String[] permissions,
                                                  String title,
                                                  String message,
                                                  Runnable onGranted) {

        if (arePermissionsGranted(activity, permissions)) {
            onGranted.run();
            return;
        }

        ActivityResultLauncher<String[]> launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        onGranted.run();
                    } else {
                        showPermissionInfo(activity, title, message);
                    }
                }
        );

        launcher.launch(permissions);
    }

}