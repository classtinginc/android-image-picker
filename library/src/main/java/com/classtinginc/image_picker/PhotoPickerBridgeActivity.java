package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.classtinginc.library.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class PickerBridgeActivity extends AppCompatActivity {

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getIntent().getIntExtra(Extra.STYLE, R.style.AppTheme_NoActionBar));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_bridge);
        int maxSize = getIntent().getIntExtra(Extra.MAX_SIZE, 0);
        int availableSize = getIntent().getIntExtra(Extra.AVAILABLE_SIZE, 0); // TODO 이건뭐지?
        boolean allowMultiple = getIntent().getBooleanExtra(Extra.ALLOW_MULTIPLE, false);

        // TODO 옵션에 따라 사진선택 도구 오픈
        //  영상 사진 옵션에 따른 구분 필요
        if (allowMultiple) {
            pickMedia =
                    registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(maxSize), uris -> {
                        if (!uris.isEmpty()) {
                            ArrayList<Image> selectedImages;
                            Log.d("PhotoPicker", "Selected URIS: " + uris);
                            selectedImages = getImagesFromUriList(this, uris);
                            Gson gson = new GsonBuilder().create();
                            Intent intent = new Intent();
                            intent.putExtra(Extra.DATA, gson.toJson(selectedImages));
                            setResult(Activity.RESULT_OK, intent);
                        } else {
                            Log.d("PhotoPicker", "No media selected");
                        }
                        finish();
                    });
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            pickMedia =
                    registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                        ArrayList<Image> selectedImages;
                        if (uri != null) {
                            Log.d("PhotoPicker", "Selected URI: " + uri);
                            selectedImages = getImageFromUri(this, uri);
                            Gson gson = new GsonBuilder().create();
                            Intent intent = new Intent();
                            intent.putExtra(Extra.DATA, gson.toJson(selectedImages));
                            setResult(Activity.RESULT_OK, intent);
                        } else {
                            Log.d("PhotoPicker", "No media selected");
                        }
                        finish();
                    });
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }

    }

    ArrayList<Image> getImagesFromUriList(Context context, List<Uri> uriList) {
        ArrayList<Image> images = new ArrayList<>();

        for (Uri uri : uriList) {
            images.addAll(getImageFromUri(context, uri));
        }

        return images;
    }

    ArrayList<Image> getImageFromUri(Context context, Uri uri) {
        ArrayList<Image> images = new ArrayList<>();
        try {
            Cursor imageCursor = context.getContentResolver().query(
                    uri,
                    ImageUtils.proj, null, null, null);

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

                    if (thumbsImageID != null && thumbsAbsPath != null) {
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


}
