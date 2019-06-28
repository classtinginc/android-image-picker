package com.classtinginc.image_picker.models;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by classting on 28/06/2019.
 */

@Data
@EqualsAndHashCode(of = {"thumbId"})
public class Image implements Serializable {

    private String thumbId;
    private String thumbPath;
    private int selectedIndex = -1;

    public Image(String thumbId, String thumbPath) {
        this.thumbId = thumbId;
        this.thumbPath = thumbPath;
    }
}
