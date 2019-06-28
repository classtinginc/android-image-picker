package com.classtinginc.image_picker.folders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.classtinginc.image_picker.utils.Validation;

import java.util.ArrayList;

/**
 * Created by classting on 28/06/2019.
 */

class LocalFoldersPresenter {

    private LocalFoldersView view;

    LocalFoldersPresenter(LocalFoldersView view) {
        this.view = view;
    }

    void showFolders(Context context) {
        view.loadViews(getFolders(context));
    }

    ArrayList<Folder> getFolders(Context context) {
        ArrayList<Folder> folders = new ArrayList<>();

        try {
            Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ImageUtils.proj,
                    null,
                    null,
                    null);

            if (imageCursor != null && imageCursor.moveToFirst()) {
                String thumbsID;
                String thumbsImageID;
                String thumbsAbsPath;//,data,imgSize,thumbsFolderName;

                int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

                do {
                    thumbsID = imageCursor.getString(thumbsIDCol);
                    thumbsAbsPath = imageCursor.getString(thumbsDataCol);
                    thumbsImageID = imageCursor.getString(thumbsImageIDCol);

                    if (thumbsImageID != null) {

                        int i;
                        for (i = 0; i < folders.size(); i++) {
                            if (folders.get(i).getPath().equalsIgnoreCase(thumbsAbsPath.substring(0, thumbsAbsPath.lastIndexOf("/") + 1))) {
                                if (Validation.isNotEmpty(thumbsID) && Validation.isNotEmpty(thumbsAbsPath)) {
                                    folders.get(i).setSize(folders.get(i).getSize() + 1);
                                    folders.get(i).setThumbPath(thumbsAbsPath);
                                }
                                break;
                            }
                        }

                        if (i == folders.size()) {
                            Folder folder = new Folder(thumbsAbsPath.substring(0, thumbsAbsPath.lastIndexOf("/") + 1), thumbsAbsPath);
                            folders.add(folder);

                            if (Validation.isNotEmpty(thumbsID) && Validation.isNotEmpty(thumbsAbsPath)) {
                                folders.get(i).setSize(folders.get(i).getSize() + 1);
                            }
                        }
                    }
                } while (imageCursor.moveToNext());
            }

            if (imageCursor != null) {
                imageCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return folders;
    }
}
