package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;

import com.classtinginc.image_picker.folders.LocalFoldersActivity;

/**
 * Created by classting on 02/07/2019.
 */

public class ImagePicker {
    private Activity activity;

    public ImagePicker(Activity activity) {
        this.activity = activity;
    }

    public static ImagePicker with(Activity context) {
        return new ImagePicker(context);
    }

    public void startActivityForResult(int requestCode) {
        Intent intent = new Intent(activity, LocalFoldersActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
}
