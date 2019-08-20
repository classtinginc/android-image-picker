package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.folders.LocalFoldersActivity;

/**
 * Created by classting on 02/07/2019.
 */

public class ImagePicker {

    private Activity activity;
    private int style;
    private int maxSize;
    private int availableSize;
    private boolean allowMultiple;

    public ImagePicker(Activity activity) {
        this.activity = activity;
    }

    public static ImagePicker with(Activity context) {
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
            this.maxSize = 1;
        }
        return this;
    }

    public ImagePicker style(int style) {
        this.style = style;
        return this;
    }

    public void startActivityForResult(int requestCode) {
        Intent intent = new Intent(activity, LocalFoldersActivity.class);
        intent.putExtra(Extra.STYLE, style);
        intent.putExtra(Extra.MAX_SIZE, maxSize);
        intent.putExtra(Extra.AVAILABLE_SIZE, availableSize);
        intent.putExtra(Extra.ALLOW_MULTIPLE, allowMultiple);
        activity.startActivityForResult(intent, requestCode);
    }
}
