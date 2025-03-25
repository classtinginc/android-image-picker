package com.classtinginc.image_picker.models;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

/**
 * Created by classting on 28/06/2019.
 */

public class Media implements Serializable {
    private String path;
    private String name;
    private long duration;
    private int selectedIndex = -1;

    public Media(String path, String name) {
        this.path = path;
        this.name = name;
        this.duration = 0L;
    }

    public Media(String path, String name, Long duration) {
        this.path = path;
        this.name = name;
        this.duration = duration;
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

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public long getDuration() { return this.duration; }

    public void setDuration(long duration) { this.duration = duration; }

    public String getExtension() {
        Uri file = Uri.fromFile(new File(this.name));
        return MimeTypeMap.getFileExtensionFromUrl(file.toString());
    }

    public String getFormattedDuration() {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60));

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
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