package com.classtinginc.image_picker.folders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.classtinginc.image_picker.utils.Validation;
import com.classtinginc.image_picker.utils.FileUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by classting on 28/06/2019.
 */

class LocalFoldersPresenter {

    private LocalFoldersView view;
    private CompositeSubscription subscriptions;
    private MediaType mediaType;

    LocalFoldersPresenter(LocalFoldersView view, MediaType mediaType) {
        this.view = view;
        this.subscriptions = new CompositeSubscription();
        this.mediaType = mediaType;
    }

    void unsubscribe() {
        subscriptions.unsubscribe();
    }

    void showFolders(final Context context) {
        subscriptions.add(Observable.create(new Observable.OnSubscribe<ArrayList<Folder>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Folder>> subscriber) {
                subscriber.onNext(getFolders(context));
            }
        }).subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<ArrayList<Folder>>() {
                @Override
                public void call(ArrayList<Folder> folders) {
                    view.showFolders(folders);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                }
            }));
    }

    ArrayList<Folder> getFolders(Context context) {
        ArrayList<Folder> folders = new ArrayList<>();

        if (mediaType == MediaType.IMAGE_AND_VIDEO) {
            Uri uri = MediaStore.Files.getContentUri("external");
            String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)";
            String[] selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            };
            String sortOrder = MediaStore.Files.FileColumns.DATE_TAKEN + " ASC";

            try {
                Cursor mediaCursor = context.getContentResolver().query(
                        uri, FileUtils.proj, selection, selectionArgs, sortOrder
                );

                if (mediaCursor != null && mediaCursor.moveToFirst()) {
                    String thumbsID;
                    String thumbsImageID;
                    String thumbsAbsPath;

                    int thumbsIDCol = mediaCursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                    int thumbsDataCol = mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int thumbsImageIDCol = mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);

                    do {
                        thumbsID = mediaCursor.getString(thumbsIDCol);
                        thumbsAbsPath = mediaCursor.getString(thumbsDataCol);
                        thumbsImageID = mediaCursor.getString(thumbsImageIDCol);

                        Log.d("미디어", "" + thumbsAbsPath);

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
                    } while (mediaCursor.moveToNext());
                }

                if (mediaCursor != null) {
                    mediaCursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mediaType == MediaType.IMAGE) {
            Uri imageUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            try {
                Cursor imageCursor = context.getContentResolver().query(
                        imageUri,
                        ImageUtils.proj,
                        null,
                        null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");

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
        }

        return folders;
    }
}
