package com.classtinginc.image_picker.medias;

import com.classtinginc.image_picker.models.Media;

import java.util.ArrayList;

/**
 * Created by classting on 28/06/2019.
 */

public interface MediaPickerView {
    void showImages(ArrayList<Media> media);
    void notifyDataSetChanged();
    void updateButtonState(int selectedImagesCount);
    void done(ArrayList<Media> media);
    void cancel();
    void showCheckMaxSize(int maxSize);
}
