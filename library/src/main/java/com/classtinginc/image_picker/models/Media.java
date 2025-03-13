package com.classtinginc.image_picker.models;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

/**
 * Created by classting on 28/06/2019.
 */

public class Media implements Serializable {
    private String path;
    private String name;

    public Media(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getMediaPath() {
        return this.path;
    }

    public void setMediaPath(String path) {
        this.path = path;
    }

    public String getMediaName() {
        return this.name.replaceFirst("[.][^.]+$", "");
    }

    public void setMediaName(String name) { this.name = name; }

    public String getExtension() {
        Uri file = Uri.fromFile(new File(this.name));
        return MimeTypeMap.getFileExtensionFromUrl(file.toString());
    }

    @NonNull
    @Override
    public String toString() {
        return "Media{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
