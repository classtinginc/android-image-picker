package com.classtinginc.image_picker.utils;

import android.provider.MediaStore;

public class FileUtils {
    public static final String[] proj = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DURATION
    };
}
