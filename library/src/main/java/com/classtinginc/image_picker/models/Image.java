package com.classtinginc.image_picker.models;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.Serializable;

/**
 * Created by classting on 28/06/2019.
 */

public class Image implements Serializable {

    private String thumbId;
    private String thumbPath;
    private String thumbsImageID;
    private int selectedIndex = -1;

    public Image(String thumbId, String thumbPath, String thumbsImageID) {
        this.thumbId = thumbId;
        this.thumbPath = thumbPath;
        this.thumbsImageID = thumbsImageID;
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

    public String getImageName() {
        return this.thumbsImageID.replaceFirst("[.][^.]+$", "");
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public String getImageExtension() {
        Uri file = Uri.fromFile(new File(this.thumbPath));
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
        return fileExt;
    }
}
