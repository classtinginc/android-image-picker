package com.classtinginc.image_picker.models;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by classting on 28/06/2019.
 */

@Data
public class Folder implements Serializable {

    private String path;
    private String thumbPath;
    private int size;

    public Folder(String path, String thumbPath) {
        this.path = path;
        this.thumbPath = thumbPath;
    }
}
