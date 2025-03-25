package com.classtinginc.image_picker.utils;

/**
 * Created by classting on 28/06/2019.
 */

public class Validation {

    public static boolean isNotEmpty(String content) {
        return content != null && !content.trim().isEmpty();
    }
}
