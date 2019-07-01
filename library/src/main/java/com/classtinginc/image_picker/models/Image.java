package com.classtinginc.image_picker.models;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by classting on 28/06/2019.
 */

public class Image implements Serializable {

    @Expose
    private String thumbId;

    @Expose
    private String thumbPath;
    private int selectedIndex = -1;

    public Image(String thumbId, String thumbPath) {
        this.thumbId = thumbId;
        this.thumbPath = thumbPath;
    }

    public void setThumbId(String thumbId) {
        this.thumbId = thumbId;
    }

    public String getThumbId() {
        return this.thumbId;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getThumbPath() {
        return this.thumbPath;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }
}
