package com.classtinginc.image_picker.utils;

import android.provider.MediaStore;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.classtinginc.library.R;

/**
 * Created by classting on 28/06/2019.
 */

public class ImageUtils {

    public static final String[] proj = {
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
    };

    public static RequestOptions getDefaultOptions() {
        return new RequestOptions()
                .centerCrop()
                .placeholder(R.color.grey_300)
                .error(R.color.grey_300)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
    }
}
