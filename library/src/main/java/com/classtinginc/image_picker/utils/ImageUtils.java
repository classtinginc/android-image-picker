package com.classtinginc.image_picker.utils;

import android.provider.MediaStore;

/**
 * Created by classting on 28/06/2019.
 */

public class ImageUtils {

    public static final String[] proj = {
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };
}
