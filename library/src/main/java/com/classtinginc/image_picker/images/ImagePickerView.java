package com.classtinginc.image_picker.images;

import com.classtinginc.image_picker.models.Image;

import java.util.ArrayList;

/**
 * Created by classting on 28/06/2019.
 */

public interface ImagePickerView {
    void showImages(ArrayList<Image> images);
    void notifyDataSetChanged();
    void updateButtonState(int selectedImagesCount);
    void done(String images);
    void cancel();
    void showCheckMaxSize(int maxSize);
}
