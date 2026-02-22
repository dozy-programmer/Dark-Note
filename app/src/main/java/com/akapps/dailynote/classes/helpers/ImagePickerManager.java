package com.akapps.dailynote.classes.helpers;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerManager {

    public interface ImagePickCallback {
        void onImagesPicked(List<Uri> uris);

        void onCanceled();

        void onError(Exception e);
    }

    private final Context context;
    private final ImagePickCallback callback;
    private final ActivityResultLauncher<Intent> pickerLauncher;

    private ImagePickerManager(
            ActivityResultCaller caller,
            Context context,
            ImagePickCallback callback
    ) {
        this.context = context;
        this.callback = callback;

        pickerLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() != Activity.RESULT_OK
                            || result.getData() == null) {
                        callback.onCanceled();
                        return;
                    }

                    try {
                        List<Uri> uris = extractUris(result.getData());
                        callback.onImagesPicked(uris);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                });
    }

    public static ImagePickerManager register(
            ActivityResultCaller caller,
            Context context,
            ImagePickCallback callback
    ) {
        return new ImagePickerManager(caller, context, callback);
    }

    /**
     * Launch picker
     */
    public void openImagePicker(boolean allowMultiple) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        pickerLauncher.launch(intent);
    }

    private List<Uri> extractUris(Intent data) {

        List<Uri> uris = new ArrayList<>();

        if (data.getClipData() != null) {

            ClipData clipData = data.getClipData();

            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                persistPermission(uri);
                uris.add(uri);
            }

        } else if (data.getData() != null) {

            Uri uri = data.getData();
            persistPermission(uri);
            uris.add(uri);
        }

        return uris;
    }

    private void persistPermission(Uri uri) {
        try {
            context.getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }
    }
}