package com.classtinginc.image_picker.folders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.image_picker.utils.Validation;

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
    private static final String TAG = LocalFoldersPresenter.class.getSimpleName();

    private final LocalFoldersView view;
    private final CompositeSubscription subscriptions;
    private final MediaType mediaType;

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

    private ArrayList<Folder> getFolders(Context context) {
        ArrayList<Folder> folders = new ArrayList<>();

        if (mediaType == MediaType.IMAGE_AND_VIDEO) {
            Uri uri = MediaStore.Files.getContentUri("external");
            String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)";
            String[] selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            };
            queryMediaFolders(context, uri, selection, selectionArgs, folders);
        } else if (mediaType == MediaType.IMAGE) {
            Uri imageUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            queryMediaFolders(context, imageUri, null, null, folders);
        }
        return folders;
    }

    private void queryMediaFolders(Context context, Uri uri, String selection, String[] selectionArgs, ArrayList<Folder> folders) {
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME
                },
                selection,
                selectionArgs,
                MediaStore.Files.FileColumns.DATE_TAKEN + " ASC")) {

            if (cursor != null && cursor.moveToFirst()) {
                int dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);

                do {
                    String absPath = cursor.getString(dataCol);
                    String mediaID = cursor.getString(nameCol);

                    if (Validation.isNotEmpty(mediaID) && Validation.isNotEmpty(absPath)) {
                        String folderPath = absPath.substring(0, absPath.lastIndexOf("/") + 1);
                        addOrUpdateFolder(folders, folderPath, absPath);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Query media folders error", e);
        }
    }

    private void addOrUpdateFolder(ArrayList<Folder> folders, String folderPath, String absPath) {
        for (Folder folder : folders) {
            if (folder.getPath().equalsIgnoreCase(folderPath)) {
                folder.setSize(folder.getSize() + 1);
                folder.setThumbPath(absPath);
                return;
            }
        }
        Folder newFolder = new Folder(folderPath, absPath);
        newFolder.setSize(1);
        folders.add(newFolder);
    }
}
