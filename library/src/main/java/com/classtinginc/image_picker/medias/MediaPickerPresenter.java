package com.classtinginc.image_picker.medias;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Media;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.image_picker.utils.FileUtils;
import com.classtinginc.image_picker.utils.ImageUtils;

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

class MediaPickerPresenter {
    private static final String TAG = AppCompatActivity.class.getSimpleName();

    private final MediaPickerView view;
    private final CompositeSubscription subscriptions;
    private final ArrayList<Media> selectedMedia;
    private int maxSize;
    private boolean allowMultiple;
    private MediaType mediaType;

    MediaPickerPresenter(MediaPickerView view) {
        this.view = view;
        subscriptions = new CompositeSubscription();
        selectedMedia = new ArrayList<>();
    }

    void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    void unsubscribe() {
        subscriptions.unsubscribe();
    }

    void showImages(final Context context, final Folder folder) {
        subscriptions.add(Observable.create(new Observable.OnSubscribe<ArrayList<Media>>() {
                    @Override
                    public void call(Subscriber<? super ArrayList<Media>> subscriber) {
                        subscriber.onNext(getImages(context, folder.getPath()));
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<Media>>() {
                    @Override
                    public void call(ArrayList<Media> media) {
                        view.showImages(media);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    private ArrayList<Media> getImages(Context context, String dirPath) {
        ArrayList<Media> media = new ArrayList<>();
        String selection = MediaStore.Files.FileColumns.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + dirPath + "%"};
        String sortOrder = MediaStore.Files.FileColumns.DATE_TAKEN + " ASC";

        if (mediaType == MediaType.IMAGE_AND_VIDEO) {
            Uri uri = MediaStore.Files.getContentUri("external");
            selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?) AND " + selection;
            selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                    "%" + dirPath + "%"
            };
            queryMediaStore(context, uri, selection, selectionArgs, sortOrder, media, true);
        } else if (mediaType == MediaType.IMAGE) {
            Uri imageUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            queryMediaStore(context, imageUri, selection, selectionArgs, sortOrder, media, false);
        }
        return media;
    }

    private void queryMediaStore(Context context, Uri uri, String selection, String[] selectionArgs,
                                 String sortOrder, ArrayList<Media> media, boolean includeDuration) {
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                includeDuration ? FileUtils.proj: ImageUtils.proj,
                selection,
                selectionArgs,
                sortOrder)) {

            if (cursor != null && cursor.moveToFirst()) {
                int dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int durationCol = includeDuration ? cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION) : -1;

                do {
                    String absPath = cursor.getString(dataCol);
                    String mediaID = cursor.getString(nameCol);
                    long duration = includeDuration && durationCol != -1 ? cursor.getLong(durationCol) : 0L;

                    if (mediaID != null) {
                        media.add(0, includeDuration ? new Media(absPath, mediaID, duration) : new Media(absPath, mediaID));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Query media store error", e);
        }
    }

    void selectImage(Media media) {
        if (selectedMedia.contains(media)) {
            selectedMedia.remove(media);
            media.setSelectedIndex(-1);

            for (Media i : selectedMedia) {
                i.setSelectedIndex(selectedMedia.indexOf(i));
            }
        } else {
            if (selectedMedia.size() >= maxSize) {
                view.showCheckMaxSize(maxSize);
                return;
            }

            selectedMedia.add(media);
            media.setSelectedIndex(selectedMedia.indexOf(media));
        }

        if (allowMultiple) {
            view.updateButtonState(selectedMedia.size());
            view.notifyDataSetChanged();
        } else {
            view.done(selectedMedia);
        }
    }

    void select() {
        if (selectedMedia.isEmpty()) {
            view.cancel();
        } else {
            view.done(selectedMedia);
        }
    }
}
