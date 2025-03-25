package com.classtinginc.image_picker.images;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.io.IOException;

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
    private int availableSize;
    private boolean allowMultiple;

    ImagePickerPresenter(ImagePickerView view) {
        this.view = view;
        subscriptions = new CompositeSubscription();
        selectedImages = new ArrayList<>();
    }

    void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    void setAvailableSize(int availableSize) {
        this.availableSize = availableSize;
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
        Uri contentsUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try {
            Cursor imageCursor = context.getContentResolver().query(
                    contentsUri,
                    ImageUtils.proj,
                    MediaStore.Images.Media.DATA + " like ? ",
                    new String[] { "%" + dirPath + "%" },
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");

            if (imageCursor != null && imageCursor.moveToFirst()) {
                String thumbsID;
                String thumbsImageID;
                String thumbsAbsPath;

                int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

                do {
                    thumbsID = imageCursor.getString(thumbsIDCol);
                    thumbsAbsPath = imageCursor.getString(thumbsDataCol);
                    thumbsImageID = imageCursor.getString(thumbsImageIDCol);

                    String dirMatcher = "[^\\.]*" + dirPath.replaceAll("/", "\\\\/") + "[^/]*\\.[^\\.]*$";
                    if (thumbsAbsPath.matches(dirMatcher) && thumbsImageID != null) {
                        images.add(0, new Image(thumbsID, thumbsAbsPath, thumbsImageID));
                    }
                } while (imageCursor.moveToNext());
            }

            if (imageCursor != null) {
                imageCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            if (selectedImages.size() >= availableSize) {
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
            try {
                ImageConverter imageConverter = new ImageConverter(context);
                imageConverter.convertImageFormat(selectedImages);
            } catch (IOException ex) {
                Log.i("Classting:::", String.valueOf(ex));
            }

            Gson gson = new GsonBuilder().create();
            view.done(gson.toJson(selectedImages));
        }
    }

    void select(Context context) {
        if (selectedImages.isEmpty()) {
            view.cancel();
        } else {
            try {
                ImageConverter imageConverter = new ImageConverter(context);
                imageConverter.convertImageFormat(selectedImages);
            } catch (IOException ex) {
                Log.i("Classting:::", String.valueOf(ex));
            }

            Gson gson = new GsonBuilder().create();
            view.done(gson.toJson(selectedImages));
        }
    }
}
