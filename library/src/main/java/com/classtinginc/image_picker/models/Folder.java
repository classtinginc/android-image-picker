package com.classtinginc.image_picker.models;

import java.io.Serializable;

/**
 * Created by classting on 28/06/2019.
 */

public class Folder implements Serializable {

    private final String path;
    private String thumbPath;
    private int size;

    public Folder(String path, String thumbPath) {
        this.path = path;
        this.thumbPath = thumbPath;
    }

    public String getPath() {
        return this.path;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getThumbPath() {
        return this.thumbPath;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }
}
