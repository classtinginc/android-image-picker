package com.classtinginc.image_picker.images;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Image;
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

class ImagePickerPresenter {

    private ImagePickerView view;
    private CompositeSubscription subscriptions;
    private ArrayList<Image> selectedImages;
    private int maxSize;
    private boolean allowMultiple;
    private MediaType mediaType;

    ImagePickerPresenter(ImagePickerView view) {
        this.view = view;
        subscriptions = new CompositeSubscription();
        selectedImages = new ArrayList<>();
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
        subscriptions.add(Observable.create(new Observable.OnSubscribe<ArrayList<Image>>() {
                    @Override
                    public void call(Subscriber<? super ArrayList<Image>> subscriber) {
                        subscriber.onNext(getImages(context, folder.getPath()));
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<Image>>() {
                    @Override
                    public void call(ArrayList<Image> images) {
                        view.showImages(images);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    ArrayList<Image> getImages(Context context, String dirPath) {
        ArrayList<Image> images = new ArrayList<>();

        String dirMatcher = "[^\\.]*" + dirPath.replaceAll("/", "\\\\/") + "[^/]*\\.[^\\.]*$";

        if (mediaType == MediaType.IMAGE_AND_VIDEO) {
            Uri uri = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaStore.Files.FileColumns.DATA + " LIKE ?";
            String[] selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                    "%" + dirPath + "%"
            };
            String sortOrder = MediaStore.Files.FileColumns.DATE_TAKEN + " ASC";


            try {
                Cursor mediaCursor = context.getContentResolver().query(
                        uri, FileUtils.proj, selection, selectionArgs, sortOrder
                );

                if (mediaCursor != null && mediaCursor.moveToFirst()) {
                    String thumbsMediaID;
                    String thumbsAbsPath;
                    long thumbsMediaDuration;

                    int thumbsDataCol = mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int thumbsMediaIDCol = mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int thumbsMediaDurationCol = mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION);

                    do {
                        thumbsMediaID = mediaCursor.getString(thumbsMediaIDCol);
                        thumbsAbsPath = mediaCursor.getString(thumbsDataCol);
                        thumbsMediaDuration = mediaCursor.getLong(thumbsMediaDurationCol);

                        if (thumbsAbsPath.matches(dirMatcher) && thumbsMediaID != null) {
                            images.add(0, new Image(thumbsAbsPath, thumbsMediaID, thumbsMediaDuration));
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
                            MediaStore.Images.Media.DATA + " like ? ",
                            new String[] { "%" + dirPath + "%" },
                            MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");

                    if (imageCursor != null && imageCursor.moveToFirst()) {
                        String thumbsImageID;
                        String thumbsAbsPath;

                        int imageThumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        int imageThumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

                        do {
                            thumbsImageID = imageCursor.getString(imageThumbsImageIDCol);
                            thumbsAbsPath = imageCursor.getString(imageThumbsDataCol);

                            if (thumbsAbsPath.matches(dirMatcher) && thumbsImageID != null) {
                                images.add(0, new Image(thumbsAbsPath, thumbsImageID));
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

        return images;
    }

    void selectImage(Context context, Image image) {
        if (selectedImages.contains(image)) {
            selectedImages.remove(image);
            image.setSelectedIndex(-1);

            for (Image i : selectedImages) {
                i.setSelectedIndex(selectedImages.indexOf(i));
            }
        } else {
            if (selectedImages.size() >= maxSize) {
                view.showCheckMaxSize(maxSize);
                return;
            }

            selectedImages.add(image);
            image.setSelectedIndex(selectedImages.indexOf(image));
        }

        if (allowMultiple) {
            view.updateButtonState(selectedImages.size());
            view.notifyDataSetChanged();
        } else {
            view.done(selectedImages);
        }
    }

    void select(Context context) {
        if (selectedImages.isEmpty()) {
            view.cancel();
        } else {
            view.done(selectedImages);
        }
    }
}
