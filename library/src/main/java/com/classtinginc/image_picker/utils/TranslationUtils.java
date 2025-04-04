package com.classtinginc.image_picker.utils;

import android.content.Context;

import com.classtinginc.library.R;

/**
 * Created by classting on 28/06/2019.
 */

public class TranslationUtils {

    public static String getButtonTitle(Context context, int imagesCount) {
        if (imagesCount == 0) {
            return context.getString(R.string.btn_attach_photo, 0);
        } else if (imagesCount == 1) {
            return context.getString(R.string.btn_attach_photo, imagesCount);
        }
        return context.getString(R.string.btn_attach_photo_pl, imagesCount);
    }

    public static String getImagesCount(Context context, int imagesCount) {
        if (imagesCount <= 1) {
            return context.getString(R.string.count_photo, imagesCount);
        }
        return context.getString(R.string.count_photo_pl, imagesCount);
    }

    public static String getMaxSizeGuide(Context context, int maxSize) {
        return context.getString(R.string.toast_write_post_attach_photo_limit, maxSize);
    }

    public static String gePermissionGuide(Context context) {
        return context.getString(R.string.alert_device_permission_denied);
    }
}
