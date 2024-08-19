package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.folders.LocalFoldersActivity;

/**
 * Created by classting on 02/07/2019.
 */

public class ImagePicker {

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private int style;
    private int maxSize = 1;
    private int availableSize = 1;
    private boolean allowMultiple;

    public ImagePicker(ActivityResultLauncher<PickVisualMediaRequest> pickMedia) {
        this.pickMedia = pickMedia;
    }

    public static ImagePicker with(ActivityResultLauncher<PickVisualMediaRequest> context) {
        return new ImagePicker(context);
    }

    public ImagePicker maxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public ImagePicker availableSize(int availableSize) {
        this.availableSize = availableSize;
        return this;
    }

    public ImagePicker allowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
        if (!allowMultiple) {
            maxSize(1);
            availableSize(1);
        }
        return this;
    }

    public ImagePicker style(int style) {
        this.style = style;
        return this;
    }

    public void startActivityForResult(int requestCode) {
        // Launch the photo picker and let the user choose images and videos.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }
}
