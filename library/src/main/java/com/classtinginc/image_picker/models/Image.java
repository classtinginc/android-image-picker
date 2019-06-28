package com.classtinginc.image_picker.models;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by classting on 28/06/2019.
 */

@Data
public class Image implements Serializable {

    private String thumbId;
    private String thumbPath;

    public Image(String thumbId, String thumbPath) {
        this.thumbId = thumbId;
        this.thumbPath = thumbPath;
    }
}
