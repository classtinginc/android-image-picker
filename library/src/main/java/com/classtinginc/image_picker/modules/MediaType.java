package com.classtinginc.image_picker.modules;

import androidx.annotation.Nullable;

public enum MediaType {
    IMAGE,
    IMAGE_AND_VIDEO;

    public static MediaType fromString(@Nullable String value) {
        if (value == null) return IMAGE;

        if (value.equalsIgnoreCase("imageandvideo")) {
            return IMAGE_AND_VIDEO;
        }
        return IMAGE;
    }
}