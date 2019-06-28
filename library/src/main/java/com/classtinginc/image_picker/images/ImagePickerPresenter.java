package com.classtinginc.image_picker.images;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Image;
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

public class ImagePickerPresenter {

    private ImagePickerView view;
    private CompositeSubscription subscriptions;

    ImagePickerPresenter(ImagePickerView view) {
        this.view = view;
        subscriptions = new CompositeSubscription();
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

    public ArrayList<Image> getImages(Context context, String dirPath) {
        ArrayList<Image> images = new ArrayList<>();

        try {
            Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ImageUtils.proj,
                    MediaStore.Images.Media.DATA + " like ? ",
                    new String[] { "%" + dirPath + "%" },
                    null);

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
                        images.add(0, new Image(thumbsID, thumbsAbsPath));
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
}