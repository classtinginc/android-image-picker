package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.folders.ImagePickerActivity;
import com.classtinginc.image_picker.modules.MediaType;

/**
 * Created by classting on 02/07/2019.
 */

public class ImagePicker {
    private final Activity activity;
    private int maxSize = 1;
    private String mediaType = "image";

    public ImagePicker(Activity activity) {
        this.activity = activity;
    }

    public static ImagePicker with(Activity context) {
        return new ImagePicker(context);
    }

    public ImagePicker mediaType(@Nullable String mediaTypeStr) {
        this.mediaType = mediaTypeStr;
        return this;
    }

    public ImagePicker maxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public void startActivityForResult(int requestCode) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(Extra.MAX_SIZE, maxSize);
        intent.putExtra(Extra.MEDIA_TYPE, mediaType);
        activity.startActivityForResult(intent, requestCode);
    }
}
