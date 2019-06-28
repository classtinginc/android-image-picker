package com.classtinginc.image_picker.models;

import lombok.Data;

/**
 * Created by classting on 28/06/2019.
 */

@Data
public class Folder {

    private String path;
    private String thumbPath;
    private int size;

    public Folder(String path, String thumbPath) {
        this.path = path;
        this.thumbPath = thumbPath;
    }
}
